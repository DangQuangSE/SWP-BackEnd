package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@SecurityRequirement(name = "api")
public class ProfileAPI {

    @Autowired
    UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UserDTO request) {
        UserDTO updatedUser = userService.updateUserProfile(request);
        return ResponseEntity.ok(updatedUser);
    }
}
