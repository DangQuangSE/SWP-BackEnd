package com.S_Health.GenderHealthCare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class OTPService {

    @Autowired
    private CacheManager cacheManager;

    public void saveOtp(String email, String otp) {
        Cache cache = cacheManager.getCache("otpCache");
        cache.put(email, otp);
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
