package com.S_Health.GenderHealthCare.dto.response.feedback;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceFeedbackResponse {
    Long id;
    double rating;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
    Long appointmentId;
    String serviceFeedbackName;
    String customerName;

    List<ConsultantFeedbackResponse> consultantFeedbacks;
}
