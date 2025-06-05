package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class JwtResponse {
     String jwt;
     UserDTO user;
     String loginProvider;
}
