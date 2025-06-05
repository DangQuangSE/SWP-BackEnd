package com.S_Health.GenderHealthCare.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class PasswordRequest {
     String email;
     String otp;
     @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
             message = "Mật khẩu có ít nhất 8 ký tự, bao gồm ít nhất một chữ cái và một chữ số!")
     String password;
     String confirmPassword;
}
