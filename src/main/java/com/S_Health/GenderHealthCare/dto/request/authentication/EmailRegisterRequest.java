package com.S_Health.GenderHealthCare.dto.request.authentication;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailRegisterRequest {
   @Email(message = "Email không hợp lệ!")
    String email;
}
