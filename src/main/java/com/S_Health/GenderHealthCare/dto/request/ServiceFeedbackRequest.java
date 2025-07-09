package com.S_Health.GenderHealthCare.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceFeedbackRequest {
    double rating;
    String comment;
    Long appointmentId;
}
