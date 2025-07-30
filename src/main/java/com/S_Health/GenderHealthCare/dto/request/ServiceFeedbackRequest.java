package com.S_Health.GenderHealthCare.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceFeedbackRequest {
    @Min(value = 1, message = "Rating phải từ 1 đến 5")
    @Max(value = 5, message = "Rating phải từ 1 đến 5")
    double rating;

    @Size(max = 1000, message = "Comment không được vượt quá 1000 ký tự")
    String comment;

    @Size(max = 1000, message = "Comment consultant không được vượt quá 1000 ký tự")
    String commentConsultant;

    @NotNull(message = "Appointment ID không được để trống")
    @Positive(message = "Appointment ID phải là số dương")
    Long appointmentId;
}
