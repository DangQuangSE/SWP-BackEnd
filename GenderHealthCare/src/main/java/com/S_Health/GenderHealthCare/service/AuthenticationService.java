package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.JwtReponse;
import com.S_Health.GenderHealthCare.dto.LoginRequest;
import com.S_Health.GenderHealthCare.dto.OAuthLoginRequest;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep1;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private JWTService jWTService;

    final RestTemplate restTemplate = new RestTemplate();

    public User registerStep1(RegisterRequestStep1 request) {
        // Kiểm tra trùng số điện thoại
        if (authenticationRepository.existsByPhone(request.getPhone())) {
            throw new AuthenticationException("Số điện thoại đã tồn tại!");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthenticationException("Mật khẩu không khớp");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .isActice(true)
                .isVerify(false)
                .build();

        return authenticationRepository.save(user);
    }

    public User registerStep2(RegisterRequestStep2 request, String phone) {
        User user = authenticationRepository.findByPhone(phone)
                .orElseThrow(() -> new AuthenticationException("Không tìm thấy số điện thoại"));

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
    public User login(LoginRequest request) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getPhone(),
                    request.getPassword()
            ));
        }catch (Exception e) {
            //sai thông tin đăng nhập
            System.out.println("thông tin đăng nhập không chính xác");

            throw new AuthenticationException("invalid phone or password");
        }

        return  authenticationRepository.findUserByPhone(request.getPhone());
    }

    public String loginWithFacebook(String accessToken) {

        String fbGraphUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;

        ResponseEntity<String> response = restTemplate.getForEntity(fbGraphUrl, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Invalid Facebook token");
        }

        JSONObject fbUser = new JSONObject(response.getBody());
        String fbId = fbUser.optString("id");
        String name = fbUser.optString("name");
        String email = fbUser.optString("email");

        System.out.println("Facebook user: " + name + " - " + email + " - " + fbId);


        // Kiểm tra hoặc tạo user trong DB
        User user = authenticationRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullname(name);
            newUser.setPassword("");
            newUser.setRole(UserRole.CUSTOMER);// không cần mật khẩu
            return authenticationRepository.save(newUser);
        });

        // Tạo JWT token
        String token = jWTService.generateToken(user);

        return token;
    }




    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        return authenticationRepository.findUserByPhone(phone);
    }
}
