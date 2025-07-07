package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.ChatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Chat Session DTO")
public class ChatSessionDTO {
    
    @Schema(description = "Session ID")
    Long id;
    
    @Schema(description = "Unique session identifier")
    String sessionId;
    
    @Schema(description = "Customer name")
    String customerName;
    
    @Schema(description = "Staff name handling this session")
    String staffName;
    
    @Schema(description = "Chat status")
    ChatStatus status;
    
    @Schema(description = "Created time")
    LocalDateTime createdAt;
    
    @Schema(description = "Last updated time")
    LocalDateTime updatedAt;
    
    @Schema(description = "Last message")
    String lastMessage;
    
    @Schema(description = "Unread message count")
    Integer unreadCount;
    
    @Schema(description = "Chat messages")
    List<ChatMessageDTO> messages;
}
