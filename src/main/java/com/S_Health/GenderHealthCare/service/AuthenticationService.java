package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.LoginEmailRequest;
import com.S_Health.GenderHealthCare.dto.request.PasswordRequest;
import com.S_Health.GenderHealthCare.dto.response.JwtResponse;
import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    OTPService otpService;
    @Autowired
    JWTService jwtService;
    @Value("${google.client.id}")
    String googleClientId;
    @Autowired
    JWTService jWTService;
    @Autowired
    ModelMapper modelMapper;


    final RestTemplate restTemplate = new RestTemplate();

    public boolean checkExistEmail(String email){
        return authenticationRepository.existsByEmail(email);
    }
    public void setPassword(PasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Mật khẩu không khớp!");
        }
        String password = passwordEncoder.encode(request.getPassword());
        authenticationRepository.save(User.builder()
                .email(request.getEmail())
                .password(password)
                .isVerify(true)
                .isActive(true)
                .role(UserRole.CUSTOMER)
                .build());
        otpService.removeOtp(request.getEmail());
    }

    public JwtResponse loginWithEmail(LoginEmailRequest loginEmailRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginEmailRequest.getEmail(),
                    loginEmailRequest.getPassword()
            ));
        } catch (Exception e) {
            System.out.println("Thông tin đăng nhập không chính xác!");
            throw new AuthenticationException("Email hoặc mật khẩu không chính xác!");
        }
        User user = authenticationRepository.findUserByEmail(loginEmailRequest.getEmail());
        String jwt = jwtService.generateToken(user);
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return new JwtResponse(jwt, userDTO, "email");
    }

    public JwtResponse loginWithGoogleToken(String googleToken) {
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
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            return new JwtResponse(jwt, userDTO, "google");
        } catch (Exception e) {
            throw new AuthenticationException("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }


//    public OAuthLoginResponse loginWithFacebook(String accessToken) {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken); // chuẩn: Authorization: Bearer <token>
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        // 2. Gọi Graph API để lấy thông tin user
//        String url = "https://graph.facebook.com/me?fields=id,name,email";
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                entity,
//                String.class
//        );
//        System.out.println("Access token from frontend: " + accessToken);
//
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("Token Facebook không hợp lệ");
//        }
//
//        JSONObject fbUser = new JSONObject(response.getBody());
//        String fbId = fbUser.optString("id");
//        String name = fbUser.optString("name");
//        String email = fbUser.optString("email");
//
//        System.out.println("Facebook user: " + name + " - " + email + " - " + fbId);
//
//        // Kiểm tra hoặc tạo user trong DB
//        User user = authenticationRepository.findByEmail(email).orElseGet(() -> {
//            User newUser = new User();
//            newUser.setEmail(email);
//            newUser.setFullname(name);
//            newUser.setPassword("");
//            newUser.setActive(true);
//            newUser.setVerify(true);
//            newUser.setRole(UserRole.CUSTOMER);// không cần mật khẩu
//            return authenticationRepository.save(newUser);
//        });
//
//        // Tạo JWT token
//        String token = jwtService.generateToken(user);
//
//        return new OAuthLoginResponse(token, user.fullname, user.email, true);
//    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authenticationRepository.findUserByEmail(email);
    }

}
