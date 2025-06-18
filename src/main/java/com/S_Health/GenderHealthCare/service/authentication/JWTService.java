package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String jwtSecret;
    private SecretKey key;
    @Autowired
    private AuthenticationRepository authenticationRepository;
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
    public Claims extractAllClaims(String token) {
        return  Jwts.parser().
                verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public User extractAccount (String token){
        String email = extractClaim(token, Claims::getSubject);
        return authenticationRepository.findUserByEmail(email);
    }
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    // get Expiration form CLAIM
    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    // from claim and extract specific data type.
    public <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return  resolver.apply(claims);
    }
}
