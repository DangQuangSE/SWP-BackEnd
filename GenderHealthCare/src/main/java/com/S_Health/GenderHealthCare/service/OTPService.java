package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OTPService {

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationRepository authenticationRepository;

    public void generateOTP(String email , boolean isForgotPassword){
        if(isForgotPassword){
            if(!authenticationRepository.existsByEmail(email)){
                throw new RuntimeException("Email does not exist");
            }
        }else {
            if(authenticationRepository.existsByEmail(email)){
                throw new RuntimeException("Email đã tồn tại!");
            }
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        Cache cache = cacheManager.getCache("otpCache");
        cache.put(email, otp);
        emailService.sendOtp(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        Cache cache = cacheManager.getCache("otpCache");
        String cachedOtp = cache.get(email, String.class);
        return otp.equals(cachedOtp);
    }

    public void removeOtp(String email) {
        cacheManager.getCache("otpCache").evict(email);
    }
}
