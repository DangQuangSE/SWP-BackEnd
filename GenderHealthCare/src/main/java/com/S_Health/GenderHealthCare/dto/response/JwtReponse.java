package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.request.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
    @AllArgsConstructor
public class JwtReponse {
    private String jwt;
    private UserDTO user;
    private String loginProvider;
}
