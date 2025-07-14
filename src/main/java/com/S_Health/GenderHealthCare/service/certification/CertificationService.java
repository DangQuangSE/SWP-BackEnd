package com.S_Health.GenderHealthCare.service.certification;

import com.S_Health.GenderHealthCare.dto.response.certification.CertificationResponse;
import com.S_Health.GenderHealthCare.entity.Certification;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.CertificationRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.service.cloudinary.CloudinaryService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CertificationService {
    
    @Autowired
    CertificationRepository certificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    AuthUtil authUtil;
    
    public CertificationResponse createCertification(String name, MultipartFile image) {
        User currentUser = authUtil.getCurrentUser();

        // Kiểm tra user có phải là consultant không
        if (!UserRole.CONSULTANT.equals(currentUser.getRole())) {
            throw new AuthenticationException("Chỉ bác sĩ mới có thể thêm chứng chỉ");
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadCertificationImage(image);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        Certification certification = Certification.builder()
                .name(name)
                .image(imageUrl)
                .consultant(currentUser)
                .isActive(true)
                .build();

        Certification saved = certificationRepository.save(certification);

        return mapToResponse(saved);
    }
    
    public CertificationResponse updateCertification(Long id, String name, MultipartFile image) {
        User currentUser = authUtil.getCurrentUser();

        Certification certification = certificationRepository.findByIdAndConsultantAndIsActiveTrue(id, currentUser)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chứng chỉ hoặc bạn không có quyền chỉnh sửa"));

        // Upload hình ảnh mới nếu có
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadCertificationImage(image);
                certification.setImage(imageUrl);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        // Cập nhật thông tin
        certification.setName(name);
        Certification updated = certificationRepository.save(certification);

        return mapToResponse(updated);
    }
    
    public List<CertificationResponse> getMyCertifications() {
        User currentUser = authUtil.getCurrentUser();
        
        if (!UserRole.CONSULTANT.equals(currentUser.getRole())) {
            throw new AuthenticationException("Chỉ bác sĩ mới có thể xem chứng chỉ");
        }
        
        List<Certification> certifications = certificationRepository.findByConsultantAndIsActiveTrue(currentUser);
        
        return certifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    

    
    public void deleteCertification(Long id) {
        User currentUser = authUtil.getCurrentUser();
        
        Certification certification = certificationRepository.findByIdAndConsultantAndIsActiveTrue(id, currentUser)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy chứng chỉ hoặc bạn không có quyền xóa"));
        
        certification.setActive(false);
        certificationRepository.save(certification);
    }
    
    private CertificationResponse mapToResponse(Certification certification) {
        return CertificationResponse.builder()
                .id(certification.getId())
                .name(certification.getName())
                .imageUrl(certification.getImage())
                .consultantId(certification.getConsultant().getId())
                .consultantName(certification.getConsultant().getFullname())
                .build();
    }
}
