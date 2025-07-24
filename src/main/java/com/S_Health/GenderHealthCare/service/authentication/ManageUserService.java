package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.dto.request.authentication.CreateUserRequest;
import com.S_Health.GenderHealthCare.dto.response.ConsultantDTO;
import com.S_Health.GenderHealthCare.dto.response.CreateUserResponse;
import com.S_Health.GenderHealthCare.entity.Certification;
import com.S_Health.GenderHealthCare.entity.ConsultantFeedback;
import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.CertificationRepository;
import com.S_Health.GenderHealthCare.repository.ConsultantFeedbackRepository;
import com.S_Health.GenderHealthCare.repository.SpecializationRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    ConsultantFeedbackRepository consultantFeedbackRepository;
    @Autowired
    CertificationRepository certificationRepository;


    public CreateUserResponse createStaffAccount(CreateUserRequest request) {
        if (authenticationRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email đã tồn tại trong hệ thống");
        }
        validateRole(request.getRole());
        List<Specialization> specializations = null;
        if (request.getRole() == UserRole.CONSULTANT) {
            if (request.getSpecializationIds() == null || request.getSpecializationIds().isEmpty()) {
                throw new AppException("Tư vấn viên phải có ít nhất một chuyên môn");
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
            throw new AppException("Không thể tạo tài khoản Khách hàng qua API này");
        }
    }

    private List<Specialization> validateAndGetSpecializations(Set<Long> specializationIds) {
        List<Specialization> specializations = specializationRepository.findAllById(specializationIds);
        if (specializations.size() != specializationIds.size()) {
            throw new AppException("Một hoặc nhiều chuyên môn không tồn tại");
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
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với ID: " + userId));

        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new AppException("Người dùng này không phải là tư vấn viên");
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
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với ID: " + userId));
        // Kiểm tra xem người dùng có phải là tư vấn viên không
        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new AppException("Người dùng này không phải là tư vấn viên");
        }
        // Kiểm tra xem chuyên môn có tồn tại không
        Specialization specialization = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new AppException("Không tìm thấy chuyên môn với ID: " + specializationId));
        // Kiểm tra xem tư vấn viên có chuyên môn này không
        if (consultant.getSpecializations() == null || !consultant.getSpecializations().contains(specialization)) {
            throw new AppException("Tư vấn viên không có chuyên môn này");
        }
        // Đảm bảo tư vấn viên có ít nhất một chuyên môn sau khi xóa
        if (consultant.getSpecializations().size() <= 1) {
            throw new AppException("Tư vấn viên phải có ít nhất một chuyên môn");
        }
        // Xóa chuyên môn
        consultant.getSpecializations().remove(specialization);
        authenticationRepository.save(consultant);
    }

    public List<Specialization> getConsultantSpecializations(Long userId) {
        User consultant = authenticationRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với ID: " + userId));
        // Kiểm tra xem người dùng có phải là tư vấn viên không
        if (consultant.getRole() != UserRole.CONSULTANT) {
            throw new AppException("Người dùng này không phải là tư vấn viên");
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

    private ConsultantDTO convertToConsultantDTO(User user) {
        ConsultantDTO consultantDTO = modelMapper.map(user, ConsultantDTO.class);

        if (user.getSpecializations() != null && !user.getSpecializations().isEmpty()) {
            List<Long> specializationIds = user.getSpecializations().stream()
                    .map(Specialization::getId)
                    .collect(Collectors.toList());
            consultantDTO.setSpecializationIds(specializationIds);
        }

        double avgRating = consultantFeedbackRepository
                .findByConsultantId(user.getId())
                .stream()
                .mapToDouble(ConsultantFeedback::getRating)
                .average()
                .orElse(0.0);
        consultantDTO.setRating(avgRating);

        List<String> certNames = certificationRepository
                .findByConsultantAndIsActiveTrue(user)
                .stream()
                .map(Certification::getName)
                .collect(Collectors.toList());
        consultantDTO.setCertificationNames(certNames);
        return consultantDTO;
    }



    public List<ConsultantDTO> getConsultantsByService(Long serviceId) {
        List<Specialization> specializations = specializationRepository.findByServicesIdAndIsActiveTrue(serviceId);
        List<Long> specializationIds = specializations.stream().map(Specialization::getId).toList();
        List<User> consultants = authenticationRepository.findBySpecializations_IdInAndIsActive(specializationIds, true);

        return consultants.stream()
                .filter(user -> UserRole.CONSULTANT.equals(user.getRole()))
                .map(this::convertToConsultantDTO)
                .collect(Collectors.toList());
    }

    public void softDeleteUser(Long userId) {
        User user = authenticationRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với ID: " + userId));

        user.setActive(false);
        authenticationRepository.save(user);
    }
    public void restoreUser(Long userId) {
        User user = authenticationRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với ID: " + userId));

        if (user.isActive()) {
            throw new AppException("Người dùng đang hoạt động bình thường");
        }

        user.setActive(true);
        authenticationRepository.save(user);
    }
}