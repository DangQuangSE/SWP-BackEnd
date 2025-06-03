package com.S_Health.GenderHealthCare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuthLoginResponse {
   private String accessToken;
   private String name;
   private String email;
   boolean success;
}
