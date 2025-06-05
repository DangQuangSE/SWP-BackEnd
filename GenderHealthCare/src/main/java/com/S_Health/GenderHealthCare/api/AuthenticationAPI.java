package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.EmailRegisterRequest;
import com.S_Health.GenderHealthCare.dto.request.OAuthLoginRequest;
import com.S_Health.GenderHealthCare.dto.request.PasswordRequest;
import com.S_Health.GenderHealthCare.dto.request.VerifyOTPRequest;
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
    private OTPService otpService;



    @PostMapping("/auth/request-OTP")
    public ResponseEntity loginWithEmail(@Valid @RequestBody EmailRegisterRequest request) {
        otpService.generateOTP(request.getEmail());
        return ResponseEntity.ok("OTP đã được gửi tới email!");
    }

    @PostMapping("/auth/verify-Otp")
    public ResponseEntity verifyOTP(@RequestBody VerifyOTPRequest request) {
        Boolean check = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return check ? ResponseEntity.ok("OTP hợp lệ!") : ResponseEntity.badRequest().body("OTP không hợp lệ hoặc đã hết hạn!");
    }
    @PostMapping("/auth/config-password")
    public ResponseEntity setPassword(@Valid @RequestBody PasswordRequest request){
            authenticationService.setPassword(request);
            return ResponseEntity.ok("Thiết lập mật khẩu thành công!");
    }
    @PostMapping("/auth/google")
    public ResponseEntity loginWithGoogle(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authenticationService.loginWithGoogleToken(request.getAccessToken()));
    }
//    @PostMapping("/register-step1")
//    public ResponseEntity registerStep1(@Valid @RequestBody RegisterRequestStep1 request){
//        return ResponseEntity.ok(authenticationService.registerStep1(request));
//    }
//    @PostMapping("/register-step2")
//    public ResponseEntity registerStep2( @Valid @RequestBody RegisterRequestStep2 request, @RequestParam String phone){
//        return ResponseEntity.ok(authenticationService.registerStep2(request, phone));
//    }


    //login facebook
//    @PostMapping("/auth/facebook")
//    public ResponseEntity loginFacebook(@RequestBody OAuthLoginRequest request) {
//        System.out.println("Access token nhận từ frontend: " + request.getAccessToken());
//        return ResponseEntity.ok(authenticationService.loginWithFacebook(request.getAccessToken()));
//    }
}
