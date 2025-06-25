package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.service.CycleTrackingRequest;
import com.S_Health.GenderHealthCare.dto.response.CycleTrackingResponse;
import com.S_Health.GenderHealthCare.service.CycleTrackingService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cycle-track")
@SecurityRequirement(name = "api")
public class CycleTrackingAPI {
    @Autowired
    private CycleTrackingService cycleTrackingService;
    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/log")
    public ResponseEntity<CycleTrackingResponse> saveDailyLog(@RequestBody CycleTrackingRequest request) {
        System.out.println("tesssssssssss");
        CycleTrackingResponse response = cycleTrackingService.saveDailyLog(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<CycleTrackingRequest>> getLogs() {
        Long userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(cycleTrackingService.getLogsByUser(userId));
    }
}
