package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.response.certification.CertificationResponse;
import com.S_Health.GenderHealthCare.service.certification.CertificationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@SecurityRequirement(name = "api")
public class CertificationAPI {

    @Autowired
    CertificationService certificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificationResponse> createCertification(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("image") MultipartFile image) {

        CertificationResponse response = certificationService.createCertification(name, image);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificationResponse> updateCertification(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        CertificationResponse response = certificationService.updateCertification(id, name, image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-certifications")
    public ResponseEntity<List<CertificationResponse>> getMyCertifications() {
        List<CertificationResponse> certifications = certificationService.getMyCertifications();
        return ResponseEntity.ok(certifications);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.ok("Xóa chứng chỉ thành công");
    }
}
