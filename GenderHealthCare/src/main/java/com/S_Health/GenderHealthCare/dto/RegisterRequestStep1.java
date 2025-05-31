package com.S_Health.GenderHealthCare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequestStep1 {
   @Pattern(regexp = "0(3|5|7|8|9)+[0-9]{8}", message = "Phone invalid!")
   private String phone;
   @NotBlank(message = "Password must be not blank")
   private String password;
   private String confirmPassword;
}
