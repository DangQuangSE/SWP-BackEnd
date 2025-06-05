package com.S_Health.GenderHealthCare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String fullname;
    private String phone;
    private String email;
    private String imageUrl;
    private String role;
}
