package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.*;
import com.S_Health.GenderHealthCare.service.AuthenticationService;
import com.S_Health.GenderHealthCare.service.EmailService;
import com.S_Health.GenderHealthCare.service.OTPService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@SecurityRequirement(name = "/api")
public class AuthenticationAPI {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private OTPService otpService;



    @PostMapping("/auth/request-OTP")
    public ResponseEntity loginWithEmail(@Valid @RequestBody EmailRegisterRequest request) {
        if(authenticationService.checkExistEmail(request.getEmail())){
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
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

    @PostMapping("/auth/forgot-password/request-otp")
    public ResponseEntity sendForgotOtp(@RequestBody EmailRegisterRequest request) {
        if (!authenticationService.checkExistEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email chưa được đăng ký.");
        }
        otpService.generateForgotPasswordOTP(request.getEmail()); // true = quên mật khẩu
        return ResponseEntity.ok("OTP đã được gửi tới email để đặt lại mật khẩu!");
    }

    @PostMapping("/auth/forgot-password/verify-otp")
    public ResponseEntity verifyForgotOtp(@RequestBody VerifyOTPRequest request) {
        Boolean check = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return check ? ResponseEntity.ok("OTP hợp lệ!") : ResponseEntity.badRequest().body("OTP không hợp lệ hoặc đã hết hạn!");
    }

    @PostMapping("/auth/forgot-password/resetPass")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordRequest request) {
        authenticationService.setPassword(request);
        return ResponseEntity.ok("Mật khẩu đã được cập nhật thành công.");
    }

    @PostMapping("/auth/google")
    public ResponseEntity loginWithGoogle(@RequestBody OAuthLoginRequest request) {
        return ResponseEntity.ok(authenticationService.loginWithGoogleToken(request.getAccessToken()));
    }
    @PostMapping("/auth/login")
    public ResponseEntity loginWithEmail(@RequestBody LoginEmailRequest request){
        return ResponseEntity.ok(authenticationService.loginWithEmail(request));
    }


    //login facebook
    @PostMapping("/auth/facebook")
    public ResponseEntity loginFacebook(@RequestBody OAuthLoginRequest request) {
        System.out.println("Access token nhận từ frontend: " + request.getAccessToken());
        return ResponseEntity.ok(authenticationService.loginWithFacebook(request.getAccessToken()));
    }
}
