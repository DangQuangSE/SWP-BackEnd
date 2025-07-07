package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chat Message DTO")
public class ChatMessageDTO {
    
    @Schema(description = "Message ID")
    Long id;
    
    @Schema(description = "Session ID")
    String sessionId;
    
    @Schema(description = "Sender name")
    String senderName;
    
    @Schema(description = "Sender type")
    SenderType senderType;
    
    @Schema(description = "Message content")
    String message;
    
    @Schema(description = "Sent time")
    LocalDateTime sentAt;
    
    @Schema(description = "Is message read")
    Boolean isRead;
}
