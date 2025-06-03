package com.S_Health.GenderHealthCare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRegisterRequest {
   @Email(message = "Email không hợp lệ!")
   private String email;
   @NotBlank(message = "Vui lòng nhập mật khẩu!")
   private String password;
   private String confirmPassword;
}
