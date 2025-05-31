package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.RegisterRequestStep1;
import com.S_Health.GenderHealthCare.dto.RegisterRequestStep2;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
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
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
