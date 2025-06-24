package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.service.medicalProfile.MedicalProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-profile")
@SecurityRequirement(name = "api")
public class MedicalProfileAPI {
    @Autowired
    MedicalProfileService medicalProfileService;

    @GetMapping("my-profile")
    public ResponseEntity getMyProfile(@RequestParam Long serviceId) {
        return ResponseEntity.ok(medicalProfileService.getMyProfile(serviceId));
    }
    @GetMapping("/{id}/appointments")
    public ResponseEntity getAppointmentsByMedicalProfile(@PathVariable Long medicalProfileId) {
        List<AppointmentDTO> appointments = medicalProfileService.getAppointmentsByMedicalProfile(medicalProfileId);
        return ResponseEntity.ok(appointments);
    }
    // hiển thị cho user
    @GetMapping("/{profileId}/appointments")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
            @PathVariable Long profileId,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(medicalProfileService.getAppointmentByStatusAndMedicalProfile(profileId, status));
    }
    //lấy hồ sơ customer cho consultant xem
    @GetMapping("/medical-profile/consultant-view")
    public ResponseEntity viewMedicalProfileForConsultant(
            @RequestParam Long customerId,
            @RequestParam Long serviceId) {
        return ResponseEntity.ok(  medicalProfileService
                .getMedicalProfileForConsultant(customerId, serviceId));
    }
}
