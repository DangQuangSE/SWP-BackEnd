package com.S_Health.GenderHealthCare.service.schedule;

import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.response.TimeSlotDTO;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                request.getFrom(),
                request.getTo()
        );

        return schedules.stream()
                .filter(Schedule::isAvailable)
                .collect(Collectors.groupingBy(Schedule::getWorkDate))
                .entrySet().stream()
                .map(entry -> new ScheduleConsultantResponse(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(s -> new TimeSlotDTO(s.getStartTime(), s.getEndTime()))
                                .collect(Collectors.toList())
                ))
                .sorted(Comparator.comparing(ScheduleConsultantResponse::getWorkDate))
                .collect(Collectors.toList());
    }



}
