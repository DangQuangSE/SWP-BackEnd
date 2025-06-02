package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.JwtReponse;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep1;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JWTService jwtService;
    @Value("${google.client.id}")
    private String googleClientId;

    public User registerStep1(RegisterRequestStep1 request) {
        // Kiểm tra trùng số điện thoại
        if (authenticationRepository.existsByPhone(request.getPhone())) {
            throw new AuthenticationException("Phone exist!");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Confirm password not match!");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerify(false)
                .build();

        return authenticationRepository.save(user);
    }

    public User registerStep2(RegisterRequestStep2 request, String phone) {
        User user = authenticationRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("Phone not exist!"));

        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        return authenticationRepository.save(user);
    }

    public JwtReponse loginWithGoogleToken(String googleToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new AuthenticationException("Mã xác minh không chính xác!");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String imageUrl = (String) payload.get("picture");

            User user = authenticationRepository.findByEmail(email).orElseGet(() -> {
                return authenticationRepository.save(User.builder()
                        .email(email)
                        .fullname(name)
                        .imageUrl(imageUrl)
                        .isVerify(true)
                        .isActive(true)
                        .role(UserRole.CUSTOMER)
                        .build());
            });

            String jwt = jwtService.generateToken(user);
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getFullname(),
                    user.getPhone(),
                    user.getEmail(),
                    user.getImageUrl(),
                    user.getRole().name());
            return new JwtReponse(jwt, userDTO, "google");
        } catch (Exception e) {
            throw new AuthenticationException("Đăng nhập Google thất bại " + e.getMessage());
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
