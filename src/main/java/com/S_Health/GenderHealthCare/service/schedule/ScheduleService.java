package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleRegisterRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import com.S_Health.GenderHealthCare.dto.response.ScheduleRegisterResponse;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.ScheduleRepository;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;

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
}
