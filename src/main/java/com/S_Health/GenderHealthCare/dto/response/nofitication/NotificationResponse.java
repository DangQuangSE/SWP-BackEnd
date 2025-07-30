package com.S_Health.GenderHealthCare.dto.response.nofitication;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String title;
    String content;
    Boolean isRead = false;
    Boolean isActive = true;
    String type;
    LocalDateTime createdAt;
    LocalDateTime readAt;
    NotificationAppointmentResponse appointment;
    NotificationCycleTrackingResponse cycleTracking;

}
