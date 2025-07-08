package com.S_Health.GenderHealthCare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chat Read Notification DTO")
public class ChatReadNotificationDTO {
    
    @Schema(description = "Session ID")
    String sessionId;
    
    @Schema(description = "Reader name")
    String readerName;
    
    @Schema(description = "Read time")
    LocalDateTime readAt;
    
    @Schema(description = "Number of messages marked as read")
    Integer messagesRead;
}
