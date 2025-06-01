package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String jwtSecret;
    private SecretKey key;

    @PostConstruct
    public void initKey() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("fullname", user.getFullname())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 ngày
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
