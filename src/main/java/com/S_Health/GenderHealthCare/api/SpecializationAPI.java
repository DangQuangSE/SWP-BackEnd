package com.S_Health.GenderHealthCare.api;


import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.dto.request.SpecializationRequest;
import com.S_Health.GenderHealthCare.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
@SecurityRequirement(name = "api")
public class SpecializationAPI {

    @Autowired
    SpecializationService specializationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách chuyên môn", description = "Lấy danh sách tất cả các chuyên môn đang hoạt động")
    public ResponseEntity<List<SpecializationDTO>> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chuyên môn theo ID", description = "Lấy thông tin chi tiết của chuyên môn theo ID")
    public ResponseEntity<SpecializationDTO> getSpecializationById(@PathVariable Long id) {
        return ResponseEntity.ok(specializationService.getSpecializationById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm chuyên môn theo tên", description = "Tìm kiếm chuyên môn theo tên (không phân biệt hoa thường)")
    public ResponseEntity<List<SpecializationDTO>> searchSpecializationsByName(@RequestParam String name) {
        return ResponseEntity.ok(specializationService.searchSpecializationsByName(name));
    }

    @PostMapping
    @Operation(summary = "Tạo chuyên môn mới", description = "Tạo chuyên môn mới")
    public ResponseEntity<SpecializationDTO> createSpecialization(@Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.createSpecialization(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật chuyên môn", description = "Cập nhật thông tin chuyên môn")
    public ResponseEntity<SpecializationDTO> updateSpecialization(
            @PathVariable Long id,
            @Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(specializationService.updateSpecialization(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa chuyên môn", description = "Xóa chuyên môn (soft delete)")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable Long id) {
        specializationService.deleteSpecialization(id);
        return ResponseEntity.noContent().build();
    }
}