package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleCancelRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleRegisterRequest;
import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleCancelResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import com.S_Health.GenderHealthCare.dto.response.ScheduleRegisterResponse;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ScheduleStatus;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.ScheduleRepository;
import com.S_Health.GenderHealthCare.service.medicalService.BookingService;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<ScheduleConsultantResponse> getScheduleOfConsultant(ScheduleConsultantRequest request) {
        List<Schedule> schedules = scheduleRepository.findByConsultantIdAndWorkDateBetween(
                request.getConsultant_id(),
                request.getRangeDate().getFrom(),
                request.getRangeDate().getTo()
        );

        return schedules.stream()
                .filter(Schedule::isAvailable)
                .collect(Collectors.groupingBy(Schedule::getWorkDate))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate workDate = entry.getKey();
                    List<TimeSlotDTO> timeSlotDTOS = new ArrayList<>();
                    for (Schedule schedule : schedules) {
                        List<LocalTime> slotStarts = TimeSlotUtils.generateSlots(schedule.getStartTime(), schedule.getEndTime(), Duration.ofMinutes(90));
                        for (LocalTime slotStart : slotStarts) {
                            timeSlotDTOS.add(new TimeSlotDTO(slotStart, slotStart.plusMinutes(90)));
                        }
                    }
                    return new ScheduleConsultantResponse(workDate, timeSlotDTOS);
                })
                .sorted(Comparator.comparing(ScheduleConsultantResponse::getWorkDate))
                .collect(Collectors.toList());
    }

    public ScheduleRegisterResponse registerSchedule(ScheduleRegisterRequest request) {
        User consultant = authenticationRepository
                .findById(request.getConsultant_id())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bác sĩ với ID = " + request.getConsultant_id()));

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
        for (ScheduleRegisterRequest.ScheduleItem item : scheduleItems) {
            Schedule schedule = new Schedule();
            schedule.setConsultant(consultant);
            schedule.setAvailable(true);
            schedule.setWorkDate(item.getWorkDate());
            schedule.setStartTime(item.getTimeSlotDTO().getStartTime());
            schedule.setEndTime(item.getTimeSlotDTO().getEndTime());
            schedules.add(schedule);
        }
        scheduleRepository.saveAll(schedules);
        //setup response
        Map<LocalDate, List<TimeSlotDTO>> mapSchedule = new HashMap<>();
        for (ScheduleRegisterRequest.ScheduleItem item : scheduleItems) {
            mapSchedule.computeIfAbsent(item.getWorkDate(), day -> new ArrayList<>())
                    .add(new TimeSlotDTO(item.getTimeSlotDTO().getStartTime(), item.getTimeSlotDTO().getEndTime()));
        }
        List<ScheduleConsultantResponse> scheduleResponse = mapSchedule.entrySet().stream()
                .map(entry -> {
                    ScheduleConsultantResponse response = new ScheduleConsultantResponse();
                    response.setWorkDate(entry.getKey());
                    response.setTimeSlotDTOs(entry.getValue());
                    return response;
                }).toList();
        ScheduleRegisterResponse scheduleRegisterResponse = new ScheduleRegisterResponse();
        scheduleRegisterResponse.setConsultant_id(request.getConsultant_id());
        scheduleRegisterResponse.setSchedules(scheduleResponse);
        return scheduleRegisterResponse;
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

