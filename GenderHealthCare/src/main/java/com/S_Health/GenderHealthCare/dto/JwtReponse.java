package com.S_Health.GenderHealthCare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class JwtReponse {
    private String jwt;
    private UserDTO user;
    private String loginProvider;
}
