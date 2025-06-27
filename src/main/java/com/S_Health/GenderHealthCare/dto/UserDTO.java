package com.S_Health.GenderHealthCare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    String fullname;
    long id;
    String phone;
    String email;
    String imageUrl;
    String role;
    LocalDate dateOfBirth;
    String address;
}
