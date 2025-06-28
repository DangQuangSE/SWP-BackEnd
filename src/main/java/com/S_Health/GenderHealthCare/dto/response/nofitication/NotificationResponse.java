package com.S_Health.GenderHealthCare.dto.response.nofitication;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String title;
    String content;
    Boolean isRead;
    String type;
    LocalDateTime createdAt;
    LocalDateTime readAt;
    NotificationAppointmentResponse appointment;
    NotificationCycleTrackingResponse cycleTracking;
}
