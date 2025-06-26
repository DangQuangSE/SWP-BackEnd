package com.S_Health.GenderHealthCare.api;


import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import com.S_Health.GenderHealthCare.dto.response.ComboResponse;
import com.S_Health.GenderHealthCare.service.MedicalService.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@SecurityRequirement(name = "api")
public class ServiceAPI {
    @Autowired
    ServiceManagementService serviceManagementService;

    @GetMapping
    @Operation(summary = "Lấy danh sách dịch vụ", description = "Lấy danh sách tất cả các dịch vụ đang hoạt động")
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        return ResponseEntity.ok(serviceManagementService.getAllServices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy dịch vụ theo ID", description = "Lấy thông tin chi tiết của dịch vụ theo ID")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceManagementService.getServiceById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm dịch vụ theo tên", description = "Tìm kiếm dịch vụ theo tên (không phân biệt hoa thường)")
    public ResponseEntity<List<ServiceDTO>> searchServicesByName(@RequestParam String name) {
        return ResponseEntity.ok(serviceManagementService.searchServicesByName(name));
    }

    @GetMapping("/specialization/{specializationId}")
    @Operation(summary = "Lấy dịch vụ theo chuyên môn", description = "Lấy danh sách dịch vụ theo ID chuyên môn")
    public ResponseEntity<List<ServiceDTO>> getServicesBySpecialization(@PathVariable Long specializationId) {
        return ResponseEntity.ok(serviceManagementService.getServicesBySpecialization(specializationId));
    }

    @PostMapping
    @Operation(summary = "Tạo dịch vụ mới", description = "Tạo dịch vụ mới với các thông tin cơ bản và chuyên môn")
    public ResponseEntity<ServiceDTO> createService(@Valid @RequestBody ServiceDTO serviceDTO) {
        return ResponseEntity.ok(serviceManagementService.createService(serviceDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật dịch vụ", description = "Cập nhật thông tin dịch vụ")
    public ResponseEntity<ServiceDTO> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceDTO serviceDTO) {
        return ResponseEntity.ok(serviceManagementService.updateService(id, serviceDTO));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Kích hoạt dịch vụ", description = "Kích hoạt dịch vụ đã bị vô hiệu hóa")
    public ResponseEntity<ServiceDTO> activateService(@PathVariable Long id) {
        return ResponseEntity.ok(serviceManagementService.activateService(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Vô hiệu hóa dịch vụ", description = "Vô hiệu hóa dịch vụ (soft delete)")
    public ResponseEntity<ServiceDTO> deactivateService(@PathVariable Long id) {
        return ResponseEntity.ok(serviceManagementService.deactivateService(id));
    }

    @PostMapping("/combo")
    @Operation(summary = "Tạo gói dịch vụ", description = "Tạo gói dịch vụ kết hợp từ nhiều dịch vụ riêng lẻ")
    public ResponseEntity<ComboResponse> createComboService(@Valid @RequestBody ServiceDTO serviceDTO) {
        return ResponseEntity.ok(serviceManagementService.createComboService(serviceDTO));
    }
}