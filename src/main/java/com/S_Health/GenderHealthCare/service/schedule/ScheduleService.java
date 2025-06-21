package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.SlotDTO;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleCancelRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleRegisterRequest;
import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleCancelResponse;
import com.S_Health.GenderHealthCare.dto.response.WorkDateSlotResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleRegisterResponse;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.ConsultantSlot;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ScheduleStatus;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.ConsultantSlotRepository;
import com.S_Health.GenderHealthCare.repository.ScheduleRepository;
import com.S_Health.GenderHealthCare.service.MedicalService.BookingService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;
    @Autowired
    ServiceSlotPoolService serviceSlotPoolService;
    @Autowired
    BookingService bookingService;
    @Autowired
    ConsultantSlotRepository consultantSlotRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;

    public List<WorkDateSlotResponse> getScheduleOfConsultant(ScheduleConsultantRequest request) {
        List<ConsultantSlot> slots = consultantSlotRepository.findByConsultantIdAndDateBetweenAndStatus(request.getConsultant_id(),
                request.getRangeDate().getFrom(),
                request.getRangeDate().getTo(), SlotStatus.ACTIVE);
        Map<LocalDate, List<SlotDTO>> slotMap = new HashMap<>();
        for (ConsultantSlot slot : slots) {
            SlotDTO slotDTO = new SlotDTO(
                    slot.getId(),
                    slot.getDate(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    slot.getMaxBooking(),
                    slot.getCurrentBooking(),
                    slot.getAvailableBooking()
            );
            slotMap.computeIfAbsent(slot.getDate(), day -> new ArrayList<>()).add(slotDTO);
        }
        return slotMap.entrySet().stream()
                .map(entry -> new WorkDateSlotResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(WorkDateSlotResponse::getWorkDate))
                .toList();
    }

    public ScheduleRegisterResponse registerSchedule(ScheduleRegisterRequest request) {
        User consultant = authenticationRepository.findById(authUtil.getCurrentUserId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người tư vấn này!"));
        List<ScheduleRegisterRequest.ScheduleItem> scheduleItems = request.getScheduleItems();
        for (ScheduleRegisterRequest.ScheduleItem item : scheduleItems) {
            if (!item.getWorkDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(item.getWorkDate() + "Ngày làm việc phải sau ngày hiện tại");
            }
        }
        Set<LocalDate> uniqueWorkDate = new HashSet<>();
        for (ScheduleRegisterRequest.ScheduleItem item : scheduleItems) {
            if (!uniqueWorkDate.add(item.getWorkDate())) {
                throw new IllegalArgumentException(item.getWorkDate() + " bị trùng trong danh sách");
            }
        }
        List<Schedule> schedules = new ArrayList<>();
        List<ConsultantSlot> consultantSlots = new ArrayList<>();
        List<ScheduleRegisterResponse.WorkDate> workDates = new ArrayList<>();
        for (ScheduleRegisterRequest.ScheduleItem item : scheduleItems) {
            Schedule schedule = new Schedule();
            schedule.setConsultant(consultant);
            schedule.setAvailable(true);
            schedule.setWorkDate(item.getWorkDate());
            schedule.setStartTime(item.getTimeSlotDTO().getStartTime());
            schedule.setEndTime(item.getTimeSlotDTO().getEndTime());
            schedule.setStatus(ScheduleStatus.ACTIVE);
            schedules.add(schedule);
            List<LocalTime> slots = TimeSlotUtils.generateSlots(item.getTimeSlotDTO().getStartTime(), item.getTimeSlotDTO().getEndTime(), Duration.ofMinutes(90));
            for (LocalTime start : slots) {
                LocalTime end = start.plusMinutes(90);
                ConsultantSlot consultantSlot = ConsultantSlot.builder()
                        .consultant(consultant)
                        .date(item.getWorkDate())
                        .startTime(start)
                        .endTime(end)
                        .availableBooking(6)
                        .maxBooking(6)
                        .currentBooking(0)
                        .status(SlotStatus.ACTIVE)
                        .build();
                consultantSlots.add(consultantSlot);
            }
            ScheduleRegisterResponse.WorkDate workDate = new ScheduleRegisterResponse.WorkDate();
            workDate.setDate(item.getWorkDate());
            workDate.setStart(item.getTimeSlotDTO().getStartTime());
            workDate.setEnd(item.getTimeSlotDTO().getEndTime());
            workDates.add(workDate);
        }
        scheduleRepository.saveAll(schedules);
        consultantSlotRepository.saveAll(consultantSlots);


        ScheduleRegisterResponse response = new ScheduleRegisterResponse();
        response.setConsultant_id(consultant.getId());
        response.setSchedules(workDates);
        return response;
    }

    //bác sĩ hủy lịch làm
    public ScheduleCancelResponse cancelSchedule(ScheduleCancelRequest request) {
        Long consultantId = request.getConsultant_id();
        LocalDateTime affectedSlot = request.getDate();
        // Lấy lịch làm việc bị ảnh hưởng
        Schedule schedule = scheduleRepository.findByConsultantId(request.getConsultant_id());
        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);
        // Tìm các cuộc hẹn bị ảnh hưởng
        List<AppointmentDetail> affectedAppointments = appointmentDetailRepository
                .findByConsultant_idAndSlotTime(consultantId, request.getDate());

        List<ScheduleCancelResponse.AffectedAppointment> affectedResponseList = new ArrayList<>();

        for (AppointmentDetail detail : affectedAppointments) {
            com.S_Health.GenderHealthCare.entity.Service service = detail.getService();
            LocalDate date = affectedSlot.toLocalDate();
            LocalTime time = affectedSlot.toLocalTime();
            // Lấy danh sách tư vấn viên có thể làm dịch vụ này
            List<User> consultants = serviceSlotPoolService.getConsultantInSpecialization(service.getId());
            // Loại bỏ consultant hiện tại (người đang hủy)
            consultants = consultants.stream()
                    .filter(c -> !(c.getId() == consultantId))
                    .toList();
            // Tìm người thay thế
            BookingRequest mockRequest = new BookingRequest();
            mockRequest.setService_id(service.getId());
            mockRequest.setPreferredDate(date);
            mockRequest.setSlot(time);
            User replacement = bookingService.findAvailableConsultant(mockRequest, consultants);
            if (replacement != null) {
                detail.setConsultant(replacement);
                appointmentDetailRepository.save(detail);
                affectedResponseList.add(new ScheduleCancelResponse.AffectedAppointment(
                        detail.getAppointment().getCustomer(),
                        date,
                        "Đã chuyển sang: " + replacement.getFullname()
                ));
            } else {
                // Không có người thay thế
                detail.setStatus(AppointmentStatus.CANCELED);
                appointmentDetailRepository.save(detail);

                affectedResponseList.add(new ScheduleCancelResponse.AffectedAppointment(
                        detail.getAppointment().getCustomer(),
                        date,
                        AppointmentStatus.CANCELED.name()
                ));
                // Optional: gửi thông báo cho người dùng
                // notificationService.notifyUser(...);
            }
        }
        return new ScheduleCancelResponse(
                "Đã xử lý " + affectedResponseList.size() + " lịch hẹn.",
                affectedResponseList
        );
    }
}

