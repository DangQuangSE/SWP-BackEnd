package com.S_Health.GenderHealthCare.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class VerifyOTPRequest {
     String email;
     String otp;
}
