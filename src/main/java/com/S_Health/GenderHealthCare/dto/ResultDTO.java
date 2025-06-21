package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultDTO {
    long id;
    String description;
    String diagnosis;
    String treatmentPlan;
    LocalDateTime createdAt;
}
