package com.S_Health.GenderHealthCare.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class OTPConfig {
    @Bean
    public CacheManager cacheManager(){
        // tạo ra 1 cache để lưu trữ tạm thời trong 30s
        //caffeine là 1 instance của thư viện caffeine - thư viện caching in memory dành cho Java
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("otpCache");
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).maximumSize(100));
        return caffeineCacheManager;
    }
}
