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
                        .isActive(true)
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
        if (request.isCancelWholeDay() && request.getSlot() != null) {
            throw new IllegalArgumentException("Không được truyền slot khi huỷ nguyên ngày.");
        }
        if (!request.isCancelWholeDay() && request.getSlot() == null) {
            throw new IllegalArgumentException("Phải truyền slot khi không huỷ nguyên ngày.");
        }

        Long consultantId = authUtil.getCurrentUserId();
        LocalDate date = request.getDate();
        List<AppointmentDetail> affectedAppointments;

        if (request.isCancelWholeDay()) {
            affectedAppointments = appointmentDetailRepository
                    .findByConsultant_idAndSlotDate(consultantId, date);
            List<ConsultantSlot> slots = consultantSlotRepository.findByConsultantIdAndDate(consultantId, date);
            if (slots.isEmpty()) {
                throw new BadRequestException("Không tìm thấy slots cần huỷ.");
            }
            for (ConsultantSlot slot : slots) {
                slot.setIsActive(false);
                slot.setStatus(SlotStatus.DEACTIVE);
            }
            consultantSlotRepository.saveAll(slots);
        } else {
            LocalDateTime slotTime = LocalDateTime.of(date, request.getSlot());
            affectedAppointments = appointmentDetailRepository
                    .findByConsultant_idAndSlotTime(consultantId, slotTime);
            Optional<ConsultantSlot> slotOpt = consultantSlotRepository
                    .findByConsultantIdAndDateAndStartTime(consultantId, date, request.getSlot());
            if (slotOpt.isPresent()) {
                ConsultantSlot slot = slotOpt.get();
                slot.setIsActive(false);
                slot.setStatus(SlotStatus.DEACTIVE);
                consultantSlotRepository.save(slot);
            } else {
                throw new BadRequestException("Không tìm thấy slot cần huỷ.");
            }
        }

        return new ScheduleCancelResponse(
                "Đã huỷ lịch thành công",
                affectedAppointments.stream().map(a -> new ScheduleCancelResponse.AffectedAppointment(
                        a.getAppointment().getCustomer(),
                        a.getSlotTime().toLocalDate(),
                        "Đã huỷ"
                )).toList()
        );
    }


}

