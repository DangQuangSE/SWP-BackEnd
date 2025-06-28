package com.S_Health.GenderHealthCare.dto.response.nofitication;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationAppointmentResponse {
    Long id;
    String doctorName;
    LocalDateTime appointmentDate;
}
