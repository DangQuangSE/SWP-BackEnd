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
        return ResponseEntity.ok(authenticationService.loginWithGoogleToken(request.getAccessToken()));
    }
//    @PostMapping("/register-step1")
//    public ResponseEntity registerStep1(@Valid @RequestBody RegisterRequestStep1 request){
//        return ResponseEntity.ok(authenticationService.registerStep1(request));
//    }
    @PostMapping("/register-step2")
    public ResponseEntity registerStep2( @Valid @RequestBody RegisterRequestStep2 request, @RequestParam String phone){
        return ResponseEntity.ok(authenticationService.registerStep2(request, phone));
    }


    //login facebook
    @PostMapping("/authFace")
    public ResponseEntity loginFacebook(@RequestBody OAuthLoginRequest request){
        System.out.println("Access token nhận từ frontend: " + request.getAccessToken());
        return ResponseEntity.ok(authenticationService.loginWithFacebook(request.getAccessToken()));
    }
}
