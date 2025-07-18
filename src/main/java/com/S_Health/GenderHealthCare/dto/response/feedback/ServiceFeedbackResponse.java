package com.S_Health.GenderHealthCare.dto.response.feedback;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceFeedbackResponse {
    Long id;
    double rating;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
    Long appointmentId;

    List<ConsultantFeedbackResponse> consultantFeedbacks;
}
