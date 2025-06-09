package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleResponse;
import com.S_Health.GenderHealthCare.entity.Schedule;
import com.S_Health.GenderHealthCare.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "/api")
public class ScheduleAPI {
    @Autowired
    ScheduleService scheduleService;

    @GetMapping("/view-schedule")
    public ResponseEntity getScheduleOfConsultant(
            @RequestParam(value = "consultant_id") long id,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today;
        LocalDate end = to != null ? to : today.plusWeeks(2);
        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                .consultant_id(id)
                .from(start)
                .to(end)
                .build();
        List<ScheduleResponse> result = scheduleService.getScheduleOfConsultant(scheduleRequest);
        return ResponseEntity.ok(result);
    }
}
