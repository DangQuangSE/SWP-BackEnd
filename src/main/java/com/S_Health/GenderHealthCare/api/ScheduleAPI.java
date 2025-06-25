package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.RangeDate;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleCancelRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleRegisterRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.WorkDateSlotResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.service.schedule.ServiceSlotPoolService;
import com.S_Health.GenderHealthCare.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@SecurityRequirement(name = "api")
public class ScheduleAPI {
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    ServiceSlotPoolService serviceSlotPoolService;

    @GetMapping("/view")
    public ResponseEntity getScheduleOfConsultant(
            @RequestParam(value = "consultant_id") long id,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today;
        LocalDate end = to != null ? to : today.plusWeeks(2);
        RangeDate rangeDate = new RangeDate(start, end);
        ScheduleConsultantRequest scheduleRequest = ScheduleConsultantRequest.builder()
                    .consultant_id(id)
                .rangeDate(rangeDate)
                .build();
        List<WorkDateSlotResponse> result = scheduleService.getScheduleOfConsultant(scheduleRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/slot-free-service")
    public ResponseEntity getAvailableSlots(
            @RequestParam Long service_id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today;
        LocalDate end = to != null ? to : today.plusWeeks(2);
        ScheduleServiceRequest request = new ScheduleServiceRequest(service_id, new RangeDate(start, end));
        ScheduleServiceResponse response = serviceSlotPoolService.getSlotFreeService(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity registerSchedule(@RequestBody ScheduleRegisterRequest request) {
        return ResponseEntity.ok(scheduleService.registerSchedule(request));
    }
    @PostMapping("/cancel")
    public ResponseEntity cancelSchedule(@RequestBody ScheduleCancelRequest request) {
        return ResponseEntity.ok(scheduleService.cancelSchedule(request));
    }
}
