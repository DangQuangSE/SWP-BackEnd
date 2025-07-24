package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.service.ConsultationResultRequest;
import com.S_Health.GenderHealthCare.dto.request.service.LabTestResultRequest;
import com.S_Health.GenderHealthCare.dto.request.service.ResultRequest;
import com.S_Health.GenderHealthCare.service.MedicalService.MedicalResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
@SecurityRequirement(name = "api")
@Tag(name = "Medical Result API", description = "API quản lý kết quả khám bệnh và xét nghiệm")
public class MedicalResultAPI {
    @Autowired
    MedicalResultService medicalResultService;

    @PostMapping("/consultation")
    @Operation(summary = "Nhập kết quả tư vấn khám bệnh",
            description = "API để bác sĩ nhập kết quả tư vấn khám bệnh")
    public ResponseEntity inputConsultationResult(@RequestBody @Valid ConsultationResultRequest request) {
        return ResponseEntity.ok(medicalResultService.saveConsultationResult(request));
    }

    @PostMapping("/lab-test")
    @Operation(summary = "Nhập kết quả xét nghiệm",
            description = "API để nhập kết quả xét nghiệm từ phòng lab")
    public ResponseEntity inputLabTestResult(@RequestBody @Valid LabTestResultRequest request) {
        return ResponseEntity.ok(medicalResultService.saveLabTestResult(request));
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
