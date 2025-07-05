package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.ChatMessageDTO;
import com.S_Health.GenderHealthCare.dto.ChatSessionDTO;
import com.S_Health.GenderHealthCare.dto.request.SendMessageRequest;
import com.S_Health.GenderHealthCare.dto.request.StartChatRequest;
import com.S_Health.GenderHealthCare.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@SecurityRequirement(name = "api")
@Tag(name = "Chat API", description = "APIs for customer support chat")
public class ChatAPI {

    @Autowired
    ChatService chatService;

    @PostMapping("/start")
    @Operation(summary = "Bắt đầu chat session", description = "Customer bắt đầu chat session mới")
    public ResponseEntity<ChatSessionDTO> startChatSession(@Valid @RequestBody StartChatRequest request) {
        ChatSessionDTO session = chatService.startChatSession(request);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/send")
    @Operation(summary = "Gửi tin nhắn", description = "Gửi tin nhắn trong chat session")
    public ResponseEntity<ChatMessageDTO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        ChatMessageDTO message = chatService.sendMessage(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/join/{sessionId}")
    @Operation(summary = "Staff join chat session", description = "Staff tham gia vào chat session")
    public ResponseEntity<ChatSessionDTO> joinChatSession(@PathVariable String sessionId) {
        ChatSessionDTO session = chatService.joinChatSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Lấy danh sách chat sessions", description = "Staff xem tất cả chat sessions")
    public ResponseEntity<List<ChatSessionDTO>> getChatSessions() {
        List<ChatSessionDTO> sessions = chatService.getChatSessionsForStaff();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Lấy tin nhắn của session", description = "Lấy tất cả tin nhắn trong một chat session")
    public ResponseEntity<List<ChatMessageDTO>> getSessionMessages(@PathVariable String sessionId) {
        List<ChatMessageDTO> messages = chatService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "Kết thúc chat session", description = "Kết thúc chat session")
    public ResponseEntity<Void> endChatSession(@PathVariable String sessionId) {
        chatService.endChatSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessions/{sessionId}/mark-read")
    @Operation(summary = "Đánh dấu tin nhắn đã đọc", description = "Đánh dấu tất cả tin nhắn trong session đã được đọc")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String sessionId,
            @RequestParam String readerName) {
        chatService.markMessagesAsRead(sessionId, readerName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions/{sessionId}/unread-count")
    @Operation(summary = "Lấy số tin nhắn chưa đọc", description = "Lấy số lượng tin nhắn chưa đọc cho một người dùng")
    public ResponseEntity<Integer> getUnreadCount(
            @PathVariable String sessionId,
            @RequestParam String readerName) {
        Integer count = chatService.getUnreadCount(sessionId, readerName);
        return ResponseEntity.ok(count);
    }
}
