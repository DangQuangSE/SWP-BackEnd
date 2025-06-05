package com.S_Health.GenderHealthCare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailRegisterRequest {
   @Email(message = "Email không hợp lệ!")
   String email;
   @NotBlank(message = "Vui lòng nhập mật khẩu!")
   String password;
   String confirmPassword;
}
