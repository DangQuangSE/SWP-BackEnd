package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.LoginRequest;
import com.S_Health.GenderHealthCare.dto.OAuthLoginRequest;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep1;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class AuthenticationAPI {
    @Autowired
    private AuthenticationService authenticationService;
    @PostMapping("/register-step1")
    public ResponseEntity registerStep1(@Valid @RequestBody RegisterRequestStep1 request){
        return ResponseEntity.ok(authenticationService.registerStep1(request));
    }
    @PostMapping("/register-step2")
    public ResponseEntity registerStep2( @Valid @RequestBody RegisterRequestStep2 request, @RequestParam String phone){
        return ResponseEntity.ok(authenticationService.registerStep2(request, phone));
    }
    @PostMapping("/api/auth/google")
    public ResponseEntity loginWithGoogle(@RequestBody OAuthLoginRequest request){
        return ResponseEntity.ok(authenticationService.loginWithGoogleToken(request.getToken()));

    //login phone
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequest request){
        User user = authenticationService.login(request);

        return ResponseEntity.ok(user);

    }

    //login facebook
    @PostMapping("/login-facebook")
    public ResponseEntity loginFacebook(@RequestBody OAuthLoginRequest request){
        return ResponseEntity.ok(authenticationService.loginWithFacebook(request.getToken()));
    }
}
