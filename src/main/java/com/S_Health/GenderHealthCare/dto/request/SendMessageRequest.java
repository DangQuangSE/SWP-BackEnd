package com.S_Health.GenderHealthCare.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request để gửi tin nhắn")
public class SendMessageRequest {
    
    @NotBlank(message = "Session ID không được để trống")
    @Schema(description = "Session ID", example = "session_123456")
    String sessionId;
    
    @NotBlank(message = "Tin nhắn không được để trống")
    @Schema(description = "Nội dung tin nhắn", example = "Xin chào, tôi cần hỗ trợ")
    String message;
    
    @NotBlank(message = "Tên người gửi không được để trống")
    @Schema(description = "Tên người gửi", example = "Nguyễn Văn A")
    String senderName;
}
