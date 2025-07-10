package com.S_Health.GenderHealthCare.dto.response.feedback;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AverageRatingResponse {
    Long serviceId;
    double averageRating;
    Long totalAppointment;
}
