package com.S_Health.GenderHealthCare.service;

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
    public void generateOTP(String email){
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
