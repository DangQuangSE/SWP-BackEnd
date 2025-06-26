package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.authentication.CreateUserRequest;
import com.S_Health.GenderHealthCare.service.authentication.ManageUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/")
@SecurityRequirement(name = "api")

public class ManagementUserAPI {
    @Autowired
    ManageUserService manageUserService;
    @PostMapping("/user")
    public ResponseEntity createAccount(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(manageUserService.createStaffAccount(request));
    }

}
