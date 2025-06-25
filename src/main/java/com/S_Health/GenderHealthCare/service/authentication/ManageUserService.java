package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.dto.request.authentication.CreateUserRequest;
import com.S_Health.GenderHealthCare.dto.response.CreateUserResponse;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManageUserService {
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    SpecializationRepository specializationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @Autowired
    ModelMapper modelMapper;
    public CreateUserResponse createStaffAccount(CreateUserRequest request) {
        if (authenticationRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống");
        }
        validateRole(request.getRole());
        List<Specialization> specializations = null;
        if (request.getRole() == UserRole.CONSULTANT) {
            if (request.getSpecializationIds() == null || request.getSpecializationIds().isEmpty()) {
                throw new BadRequestException("Tư vấn viên phải có ít nhất một chuyên môn");
            }
            specializations = validateAndGetSpecializations(request.getSpecializationIds());
        }
        String randomPassword = generateRandomPassword();
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .password(passwordEncoder.encode(randomPassword))
                .address(request.getAddress())
                .gender(request.getGender())
                .imageUrl(request.getImageUrl())
                .role(request.getRole())
                .isActive(true)
                .isVerify(true)
                .build();
        if (request.getRole() == UserRole.CONSULTANT && specializations != null) {
            user.setSpecializations(specializations);
        }

        user = authenticationRepository.save(user);
        emailService.sendWelcomeWithCredentials(user.getEmail(), randomPassword, user.getRole());
        CreateUserResponse response = modelMapper.map(user, CreateUserResponse.class);
        return response;
    }
    private void validateRole(UserRole role) {
        if (role == UserRole.CUSTOMER) {
            throw new BadRequestException("Không thể tạo tài khoản Khách hàng qua API này");
        }
    }
    private List<Specialization> validateAndGetSpecializations(Set<Long> specializationIds) {
        List<Specialization> specializations = specializationRepository.findAllById(specializationIds);
        if (specializations.size() != specializationIds.size()) {
            throw new BadRequestException("Một hoặc nhiều chuyên môn không tồn tại");
        }
        return specializations;
    }
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        return new Random().ints(10, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }
}
