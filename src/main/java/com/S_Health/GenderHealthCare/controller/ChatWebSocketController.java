package com.S_Health.GenderHealthCare.controller;

import com.S_Health.GenderHealthCare.dto.ChatMessageDTO;
import com.S_Health.GenderHealthCare.dto.request.SendMessageRequest;
import com.S_Health.GenderHealthCare.service.chat.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebSocketController {

    @Autowired
    ChatService chatService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request) {
        try {
            ChatMessageDTO message = chatService.sendMessage(request);
            
            // Send to specific session
            messagingTemplate.convertAndSend("/topic/chat/" + request.getSessionId(), message);
            
            // Send to staff dashboard
            messagingTemplate.convertAndSend("/topic/staff/messages", message);
            
        } catch (Exception e) {
            // Send error message
            messagingTemplate.convertAndSend("/topic/chat/" + request.getSessionId() + "/error", 
                "Error sending message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.join")
    public void joinSession(@Payload String sessionId) {
        try {
            // Notify others that someone joined
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/joined",
                "Someone joined the chat");
        } catch (Exception e) {
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/error",
                "Error joining session: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.markRead")
    public void markAsRead(@Payload Map<String, String> payload) {
        try {
            String sessionId = payload.get("sessionId");
            String readerName = payload.get("readerName");

            chatService.markMessagesAsRead(sessionId, readerName);
        } catch (Exception e) {
            String sessionId = payload.get("sessionId");
            messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/error",
                "Error marking messages as read: " + e.getMessage());
        }
    }
}
