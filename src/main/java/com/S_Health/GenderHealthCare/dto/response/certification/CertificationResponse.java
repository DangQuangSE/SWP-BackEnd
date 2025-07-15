package com.S_Health.GenderHealthCare.dto.response.certification;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificationResponse {

    Long id;
    String name;
    String description;
    String imageUrl;
    LocalDateTime createdAt;
    Long consultantId;
    String consultantName;
}
