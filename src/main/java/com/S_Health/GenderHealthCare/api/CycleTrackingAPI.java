package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.service.CycleTrackingRequest;
import com.S_Health.GenderHealthCare.service.CycleTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cycle-track")
public class CycleTrackingAPI {
    @Autowired
    private CycleTrackingService cycleTrackingService;

    @PostMapping("/log")
    public ResponseEntity<String> saveDailyLog(@RequestBody CycleTrackingRequest request) {
        cycleTrackingService.saveDailyLog(request);
        return ResponseEntity.ok("Lưu log chu kỳ thành công");
    }

    @GetMapping("/logs/{userId}")
    public ResponseEntity<List<CycleTrackingRequest>> getLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(cycleTrackingService.getLogsByUser(userId));
    }
}
