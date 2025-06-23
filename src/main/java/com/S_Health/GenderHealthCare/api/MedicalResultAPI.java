package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.service.MedicalService.MedicalResultService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
@SecurityRequirement(name = "api")
public class MedicalResultAPI {
    @Autowired
    MedicalResultService medicalResultService;

    @PostMapping("/result")
    public ResponseEntity inputResult(@RequestBody @Valid ResultRequest request) {
        return ResponseEntity.ok(medicalResultService.saveResult(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalResultService.getResultById(id));
    }

    @GetMapping("/appointment-detail/{id}")
    public ResponseEntity getResultsByAppointmentDetail(@PathVariable Long id) {
        return ResponseEntity.ok(medicalResultService.getAllResultsByAppointmentDetail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity updateResult(
            @PathVariable Long id,
            @Valid @RequestBody ResultRequest request) {
        return ResponseEntity.ok(medicalResultService.updateResult(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteResult(@PathVariable Long id) {
        medicalResultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}
