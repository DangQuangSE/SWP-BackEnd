package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.repository.ScheduleRepository;
import com.S_Health.GenderHealthCare.utils.TimeSlotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    @Autowired
    ScheduleRepository scheduleRepository;

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
    public 
}
