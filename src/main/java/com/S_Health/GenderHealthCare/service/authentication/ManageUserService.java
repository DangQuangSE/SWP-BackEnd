package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.dto.UserDTO;
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

import java.util.ArrayList;
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

    public User addSpecializationsToConsultant(Long userId, Set<Long> specializationIds) {
        User consultant = authenticationRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với ID: " + userId));

        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new BadRequestException("Người dùng này không phải là tư vấn viên");
        }
        List<Specialization> specializationsToAdd = validateAndGetSpecializations(specializationIds);
        if (consultant.getSpecializations() == null) {
            consultant.setSpecializations(new ArrayList<>());
        }
        for (Specialization spec : specializationsToAdd) {
            if (!consultant.getSpecializations().contains(spec)) {
                consultant.getSpecializations().add(spec);
            }
        }
        return authenticationRepository.save(consultant);
    }

    public void removeSpecializationFromConsultant(Long userId, Long specializationId) {
        User consultant = authenticationRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với ID: " + userId));
        // Kiểm tra xem người dùng có phải là tư vấn viên không
        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new BadRequestException("Người dùng này không phải là tư vấn viên");
        }
        // Kiểm tra xem chuyên môn có tồn tại không
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chuyên môn với ID: " + specializationId));
        // Kiểm tra xem tư vấn viên có chuyên môn này không
        if (consultant.getSpecializations() == null || !consultant.getSpecializations().contains(specialization)) {
            throw new BadRequestException("Tư vấn viên không có chuyên môn này");
        }
        // Đảm bảo tư vấn viên có ít nhất một chuyên môn sau khi xóa
        if (consultant.getSpecializations().size() <= 1) {
            throw new BadRequestException("Tư vấn viên phải có ít nhất một chuyên môn");
        }
        // Xóa chuyên môn
        consultant.getSpecializations().remove(specialization);
        authenticationRepository.save(consultant);
    }

    public List<Specialization> getConsultantSpecializations(Long userId) {
        User consultant = authenticationRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với ID: " + userId));
        // Kiểm tra xem người dùng có phải là tư vấn viên không
        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new BadRequestException("Người dùng này không phải là tư vấn viên");
        }
        return consultant.getSpecializations() != null ? consultant.getSpecializations() : new ArrayList<>();
    }
    public List<UserDTO> getUsersByRole(String role) {
        UserRole userRole;
        userRole = UserRole.valueOf(role.toUpperCase());
        return authenticationRepository.findByRole(userRole).stream()
                .filter(User::isActive) // Chỉ lấy user đang active
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }
    private UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user.getSpecializations() != null && !user.getSpecializations().isEmpty()) {
            List<Long> specializationIds = user.getSpecializations().stream()
                    .map(Specialization::getId)
                    .collect(Collectors.toList());
            userDTO.setSpecializationIds(specializationIds);
        }
        return userDTO;
    }

    /**
     * Xóa mềm user - Set isActive = false
     */
    public void softDeleteUser(Long userId) {
        User user = authenticationRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với ID: " + userId));

        user.setActive(false);
        authenticationRepository.save(user);
    }

    /**
     * Khôi phục user đã bị xóa mềm
     */
    public void restoreUser(Long userId) {
        User user = authenticationRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng với ID: " + userId));

        if (user.isActive()) {
            throw new BadRequestException("Người dùng đang hoạt động bình thường");
        }

        user.setActive(true);
        authenticationRepository.save(user);
    }
}