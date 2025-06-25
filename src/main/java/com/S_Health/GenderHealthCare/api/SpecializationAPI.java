package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.dto.request.SpecializationRequest;
import com.S_Health.GenderHealthCare.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialization")
@SecurityRequirement(name = "api")
@Tag(name = "Specialization", description = "API quản lý chuyên môn")
@RequiredArgsConstructor
public class SpecializationAPI {
    private final SpecializationService specializationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả chuyên môn")
    public ResponseEntity<List<SpecializationDTO>> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết một chuyên môn")
    public ResponseEntity<SpecializationDTO> getSpecializationById(@PathVariable Long id) {
        return ResponseEntity.ok(specializationService.getSpecializationById(id));
    }

    @GetMapping("/service/{serviceId}")
    @Operation(summary = "Lấy danh sách chuyên môn theo dịch vụ")
    public ResponseEntity<List<SpecializationDTO>> getSpecializationsByServiceId(
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(specializationService.getSpecializationsByServiceId(serviceId));
    }

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo mới chuyên môn")
    public ResponseEntity<SpecializationDTO> createSpecialization(
            @Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.createSpecialization(request));
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin chuyên môn")
    public ResponseEntity<SpecializationDTO> updateSpecialization(
            @PathVariable Long id,
            @Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.updateSpecialization(id, request));
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa chuyên môn")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable Long id) {
        specializationService.deleteSpecialization(id);
        return ResponseEntity.ok().build();
    }
}
