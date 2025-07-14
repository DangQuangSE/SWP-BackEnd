package com.S_Health.GenderHealthCare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    String fullname;
    long id;
    String phone;
    String email;
    MultipartFile img;
    String imageUrl;
    String role;
    LocalDate dateOfBirth;
    String address;
    List<Long> specializationIds;
}
