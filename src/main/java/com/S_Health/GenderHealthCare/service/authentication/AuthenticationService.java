package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.dto.request.authentication.LoginEmailRequest;
import com.S_Health.GenderHealthCare.dto.request.authentication.PasswordRequest;
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
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    EmailService emailService;


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
        emailService.sendWelcome(request.getEmail());
    }

    public void setPasswordForgot(PasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Mật khẩu không khớp!");
        }
        String password = passwordEncoder.encode(request.getPassword());
        User user = authenticationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Không tìm thấy người dùng với email: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        authenticationRepository.save(user);

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

        // Kiểm tra user có bị vô hiệu hóa không
        if (!user.isActive()) {
            throw new AuthenticationException("Tài khoản đã bị vô hiệu hóa!");
        }

        String jwt = jwtService.generateToken(user);
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return new JwtResponse(jwt, userDTO, "email", true);
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

            // Kiểm tra user có bị vô hiệu hóa không
            if (!user.isActive()) {
                throw new AuthenticationException("Tài khoản đã bị vô hiệu hóa!");
            }

            String jwt = jwtService.generateToken(user);
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            return new JwtResponse(jwt, userDTO, "google", true);
        } catch (Exception e) {
            throw new AuthenticationException("Đăng nhập Google thất bại: " + e.getMessage());
        }
    }

    public JwtResponse loginWithFacebook(String accessToken) {
        try{

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken); // chuẩn: Authorization: Bearer <token>
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            // 2. Gọi Graph API để lấy thông tin user
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            //log xem lỗi
            System.out.println("Access token from frontend: " + accessToken);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Token Facebook không hợp lệ");
            }

            JSONObject fbUser = new JSONObject(response.getBody());
            String fbId = fbUser.optString("id");
            String name = fbUser.optString("name");
            String email = fbUser.optString("email");
            String imageUrl = fbUser.getJSONObject("picture")
                    .getJSONObject("data")
                    .getString("url");

            System.out.println("Facebook user: " + name + " - " + email + " - " + fbId );

            // Kiểm tra hoặc tạo user trong DB
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

            // Kiểm tra user có bị vô hiệu hóa không
            if (!user.isActive()) {
                throw new AuthenticationException("Tài khoản đã bị vô hiệu hóa!");
            }

            String jwt = jwtService.generateToken(user);
            UserDTO userDTO =  modelMapper.map(user, UserDTO.class);

            return new JwtResponse(jwt, userDTO, "facebook", true);
        } catch (Exception e) {
            throw new AuthenticationException("Đăng nhập Facebook: " + e.getMessage());
        }
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authenticationRepository.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
        }

        // Kiểm tra user có bị vô hiệu hóa không
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa!");
        }

        return user;
    }

}
