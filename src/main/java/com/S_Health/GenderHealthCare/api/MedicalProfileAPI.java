package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.PatientMedicalHistoryDTO;
import com.S_Health.GenderHealthCare.dto.request.MedicalInfoUpdateRequest;
import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.service.medicalProfile.MedicalProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medical-profile")
@SecurityRequirement(name = "api")
public class MedicalProfileAPI {
    @Autowired
    MedicalProfileService medicalProfileService;

    // API cho user xem profile của mình
    @GetMapping("/my-profile")
    @Operation(summary = "Xem hồ sơ y tế của tôi")
    public ResponseEntity getMyProfile(@RequestParam Long serviceId) {
        return ResponseEntity.ok(medicalProfileService.getMyProfile(serviceId));
    }

    // API cho bác sĩ xem lịch sử bệnh nhân (simplified)
    @GetMapping("/patient/{patientId}/history")
    @Operation(summary = "Xem lịch sử khám bệnh của bệnh nhân",
               description = "Bác sĩ xem lịch sử khám bệnh cần thiết để chẩn đoán")
    public ResponseEntity<PatientMedicalHistoryDTO> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        PatientMedicalHistoryDTO history = medicalProfileService
                .getPatientHistory(patientId, page, size);
        return ResponseEntity.ok(history);
    }

    // API cho staff cập nhật thông tin y tế khi check-in
    @PutMapping("/update-medical-info")
    @Operation(summary = "Cập nhật thông tin y tế cơ bản",
               description = "Staff cập nhật thông tin y tế khi bệnh nhân check-in")
    public ResponseEntity<String> updateMedicalInfo(@RequestBody MedicalInfoUpdateRequest request) {
        medicalProfileService.updateMedicalInfo(request);
        return ResponseEntity.ok("Cập nhật thông tin y tế thành công");
    }

    // API cho bác sĩ xem thông tin y tế chi tiết
    @GetMapping("/medical-info")
    @Operation(summary = "Xem thông tin y tế chi tiết",
               description = "Bác sĩ xem thông tin y tế chi tiết của bệnh nhân")
    public ResponseEntity<MedicalProfile> getMedicalInfo(
            @RequestParam Long customerId,
            @RequestParam Long serviceId) {
        MedicalProfile profile = medicalProfileService.getMedicalInfoForDoctor(customerId, serviceId);
        return ResponseEntity.ok(profile);
    }
}
