package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.RangeDate;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleConsultantRequest;
import com.S_Health.GenderHealthCare.dto.request.schedule.ScheduleServiceRequest;
import com.S_Health.GenderHealthCare.dto.response.ScheduleConsultantResponse;
import com.S_Health.GenderHealthCare.dto.response.ScheduleServiceResponse;
import com.S_Health.GenderHealthCare.service.schedule.HospitalSlotFreeService;
import com.S_Health.GenderHealthCare.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "api")
public class ScheduleAPI {
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    HospitalSlotFreeService hospitalSlotFreeService;

    @GetMapping("/view-schedule")
    public ResponseEntity getScheduleOfConsultant(
            @RequestParam(value = "consultant_id") long id,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate start = from != null ? from : today;
        LocalDate end = to != null ? to : today.plusWeeks(2);
        ScheduleConsultantRequest scheduleRequest = ScheduleConsultantRequest.builder()
                .consultant_id(id)
                .from(start)
                .to(end)
                .build();
        List<ScheduleConsultantResponse> result = scheduleService.getScheduleOfConsultant(scheduleRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/slot-free-service")
    public ResponseEntity getAvailableSlots(
            @RequestParam Long service_id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ScheduleServiceRequest request = new ScheduleServiceRequest(service_id, new RangeDate(from, to));
        ScheduleServiceResponse response = hospitalSlotFreeService.getSlotFreeService(request);
        return ResponseEntity.ok(response);
    }
}
