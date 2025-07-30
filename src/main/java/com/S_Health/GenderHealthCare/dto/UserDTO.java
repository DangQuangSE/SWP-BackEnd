package com.S_Health.GenderHealthCare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
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
    String gender;
}
