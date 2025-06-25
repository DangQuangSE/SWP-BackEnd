package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.SpecializationDTO;
import com.S_Health.GenderHealthCare.enums.Gender;
import com.S_Health.GenderHealthCare.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
public class CreateUserResponse {
    private Long id;
    private String fullname;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private Gender gender;
    private UserRole role;
    private String imageUrl;
    private boolean isActive;
    private boolean isVerified;
    private LocalDate createdAt;
    // Chỉ có với CONSULTANT
    private List<SpecializationDTO> specializations;

}
