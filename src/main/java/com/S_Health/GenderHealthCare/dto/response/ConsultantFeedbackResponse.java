package com.S_Health.GenderHealthCare.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultantFeedbackResponse {
    Long id;
    int rating;
    Long consultantId;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
}
