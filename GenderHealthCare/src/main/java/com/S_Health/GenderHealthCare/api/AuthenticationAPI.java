package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.OAuthLoginRequest;
import com.S_Health.GenderHealthCare.dto.EmailRegisterRequest;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.service.AuthenticationService;
import com.S_Health.GenderHealthCare.service.EmailService;
import com.S_Health.GenderHealthCare.service.OTPService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class AuthenticationAPI {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OTPService otpService;

//    @PostMapping("/auth/request-Otp")
//    @PostMapping("/auth/verify-Otp")
//    @PostMapping("/auth/config-password")

    @PostMapping("/auth/google")
    public ResponseEntity loginWithGoogle(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authenticationService.loginWithGoogleToken(request.getToken()));
    }
    //login facebook
    @PostMapping("/login-facebook")
    public ResponseEntity loginFacebook(@RequestBody OAuthLoginRequest request){
        return ResponseEntity.ok(authenticationService.loginWithFacebook(request.getToken()));
    }
}
