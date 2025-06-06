package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
@Data
public class RegisterRequestStep2 {

    @NotBlank(message = "FullName must not blank")
    @Size(max = 100, message = "maximum 100 characters")
    private String fullname;

    @Email(message = "Email invalid")
    private String email;

    @Past(message = "Date Of Birth must be in the future")
    private LocalDate dateOfBirth;

    @NotNull(message = "Please choose one option")
    private Gender gender;
}
