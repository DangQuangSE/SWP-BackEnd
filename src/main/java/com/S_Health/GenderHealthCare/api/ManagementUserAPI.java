package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.authentication.CreateUserRequest;
import com.S_Health.GenderHealthCare.dto.request.authentication.UpdateConsultantSpecializationRequest;
import com.S_Health.GenderHealthCare.service.authentication.ManageUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/")
@SecurityRequirement(name = "api")

public class ManagementUserAPI {
    @Autowired
    ManageUserService manageUserService;
    
    @PostMapping("/user")
    @Operation(summary = "Tạo tài khoản nhân viên", description = "Tạo tài khoản cho nhân viên hoặc tư vấn viên")
    public ResponseEntity createAccount(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(manageUserService.createStaffAccount(request));
    }

    @PostMapping("/user/{userId}/specializations")
    @Operation(summary = "Thêm chuyên môn cho tư vấn viên", 
              description = "Thêm một hoặc nhiều chuyên môn cho tư vấn viên")
    public ResponseEntity addSpecializationsToConsultant(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateConsultantSpecializationRequest request) {
        return ResponseEntity.ok(manageUserService.addSpecializationsToConsultant(userId, request.getSpecializationIds()));
    }
    
    @DeleteMapping("/user/{userId}/specializations/{specializationId}")
    @Operation(summary = "Xóa chuyên môn của tư vấn viên", 
              description = "Xóa một chuyên môn khỏi tư vấn viên")
    public ResponseEntity removeSpecializationFromConsultant(
            @PathVariable Long userId,
            @PathVariable Long specializationId) {
        manageUserService.removeSpecializationFromConsultant(userId, specializationId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}/specializations")
    @Operation(summary = "Lấy danh sách chuyên môn của tư vấn viên", 
              description = "Lấy tất cả chuyên môn của một tư vấn viên")
    public ResponseEntity getConsultantSpecializations(@PathVariable Long userId) {
        return ResponseEntity.ok(manageUserService.getConsultantSpecializations(userId));
    }
}