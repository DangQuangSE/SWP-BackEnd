package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.OAuthLoginRequest;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep1;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthenticationAPI {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/api/register-step1")
    public ResponseEntity registerStep1(@Valid @RequestBody RegisterRequestStep1 request){
        return ResponseEntity.ok(authenticationService.registerStep1(request));
    }
    @PostMapping("/api/register-step2")
    public ResponseEntity registerStep2( @Valid @RequestBody RegisterRequestStep2 request, @RequestParam String phone){
        return ResponseEntity.ok(authenticationService.registerStep2(request, phone));
    }
    @PostMapping("/api/login-google")
    public ResponseEntity loginWithGoogle(@RequestBody OAuthLoginRequest request){
        String jwt = authenticationService.loginWithGoogleToken(request.getToken());
        return ResponseEntity.ok(Map.of("jwt", jwt));
    }
}
