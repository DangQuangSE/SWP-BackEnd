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

import java.util.Map;

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
    @GetMapping("/users")
    @Operation(summary = "Lấy danh sách người dùng theo vai trò",
            description = "Lấy danh sách tất cả người dùng theo vai trò (ADMIN, STAFF, CONSULTANT, etc.)")
    public ResponseEntity getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(manageUserService.getUsersByRole(role));
    }

    @DeleteMapping("/user/{userId}")
    @Operation(
        summary = "Vô hiệu hóa người dùng (Soft Delete)",
        description = "Vô hiệu hóa người dùng một cách an toàn với kiểm tra ràng buộc business logic. " +
                     "Người dùng sẽ không thể đăng nhập nhưng dữ liệu vẫn được bảo toàn để audit."
    )
    public ResponseEntity<?> softDeleteUser(@PathVariable Long userId) {
        manageUserService.softDeleteUser(userId);
        return ResponseEntity.ok().body(Map.of(
            "message", "Người dùng đã được vô hiệu hóa thành công",
            "userId", userId,
            "timestamp", java.time.LocalDateTime.now()
        ));
    }

    @PutMapping("/user/{userId}/restore")
    @Operation(
        summary = "Khôi phục người dùng đã bị vô hiệu hóa",
        description = "Khôi phục người dùng đã bị vô hiệu hóa trước đó, cho phép họ đăng nhập lại"
    )
    public ResponseEntity<?> restoreUser(@PathVariable Long userId) {
        manageUserService.restoreUser(userId);
        return ResponseEntity.ok().body(Map.of(
            "message", "Người dùng đã được khôi phục thành công",
            "userId", userId,
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
}