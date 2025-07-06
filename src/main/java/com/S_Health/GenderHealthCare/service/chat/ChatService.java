package com.S_Health.GenderHealthCare.service.chat;

import com.S_Health.GenderHealthCare.dto.ChatMessageDTO;
import com.S_Health.GenderHealthCare.dto.ChatSessionDTO;
import com.S_Health.GenderHealthCare.dto.request.SendMessageRequest;
import com.S_Health.GenderHealthCare.dto.request.StartChatRequest;
import com.S_Health.GenderHealthCare.entity.ChatMessage;
import com.S_Health.GenderHealthCare.entity.ChatSession;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.ChatStatus;
import com.S_Health.GenderHealthCare.enums.SenderType;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.exception.exceptions.ResourceNotFoundException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.ChatMessageRepository;
import com.S_Health.GenderHealthCare.repository.ChatSessionRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    ChatSessionRepository chatSessionRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    AuthenticationRepository authenticationRepository;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    /**
     * Customer bắt đầu chat session mới
     */
    public ChatSessionDTO startChatSession(StartChatRequest request) {
        // Tạo unique session ID
        String sessionId = "chat_" + UUID.randomUUID().toString().substring(0, 8);

        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .customerName(request.getCustomerName())
                .status(ChatStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        session = chatSessionRepository.save(session);

        // Notify staff về session mới
        notifyStaffNewSession(session);

        return convertToSessionDTO(session);
    }

    /**
     * Gửi tin nhắn trong chat session
     */
    public ChatMessageDTO sendMessage(SendMessageRequest request) {
        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        // Xác định sender type
        SenderType senderType = determineSenderType(request.getSenderName(), session);

        ChatMessage message = ChatMessage.builder()
                .chatSession(session)
                .senderName(request.getSenderName())
                .senderType(senderType)
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();

        message = chatMessageRepository.save(message);

        // Update session
        session.setUpdatedAt(LocalDateTime.now());
        if (session.getStatus() == ChatStatus.WAITING && senderType == SenderType.STAFF) {
            session.setStatus(ChatStatus.ACTIVE);
        }
        chatSessionRepository.save(session);

        ChatMessageDTO messageDTO = convertToMessageDTO(message);

        // Send realtime message
        sendRealtimeMessage(messageDTO);

        return messageDTO;
    }

    /**
     * Staff join vào chat session
     */
    public ChatSessionDTO joinChatSession(String sessionId) {
        User currentStaff = authUtil.getCurrentUser();
        if (currentStaff.getRole() != UserRole.STAFF) {
            throw new BadRequestException("Chỉ staff mới có thể join chat session");
        }

        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        session.setStaff(currentStaff);
        session.setStatus(ChatStatus.ACTIVE);
        session.setUpdatedAt(LocalDateTime.now());

        session = chatSessionRepository.save(session);

        return convertToSessionDTO(session);
    }

    /**
     * Lấy danh sách chat sessions cho staff
     */
    public List<ChatSessionDTO> getChatSessionsForStaff() {
        User currentStaff = authUtil.getCurrentUser();
        if (currentStaff.getRole() != UserRole.STAFF) {
            throw new BadRequestException("Chỉ staff mới có thể xem chat sessions");
        }

        // Lấy sessions đang WAITING và ACTIVE (không lấy ENDED) trong một query
        List<ChatStatus> activeStatuses = List.of(ChatStatus.WAITING, ChatStatus.ACTIVE);
        List<ChatSession> sessions = chatSessionRepository.findByStatusInAndIsActiveTrueOrderByUpdatedAtDesc(activeStatuses);

        return sessions.stream()
                .map(this::convertToSessionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tin nhắn của một session
     */
    public List<ChatMessageDTO> getSessionMessages(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderBySentAtAsc(session);
        return messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Kết thúc chat session
     */
    public void endChatSession(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        session.setStatus(ChatStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        chatSessionRepository.save(session);

        // Notify participants
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/ended", "Chat session ended");
    }

    /**
     * Đánh dấu tin nhắn đã đọc khi staff/customer xem
     */
    public void markMessagesAsRead(String sessionId, String readerName) {
        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        // Lấy tất cả tin nhắn chưa đọc trong session này
        List<ChatMessage> unreadMessages = chatMessageRepository
                .findByChatSessionAndIsReadFalse(session);

        // Đánh dấu đã đọc những tin nhắn không phải của người đang đọc
        unreadMessages.stream()
                .filter(message -> !message.getSenderName().equals(readerName))
                .forEach(message -> {
                    message.setIsRead(true);
                    chatMessageRepository.save(message);
                });

        // Notify real-time về việc đã đọc
        messagingTemplate.convertAndSend("/topic/chat/" + sessionId + "/read",
                Map.of("readerName", readerName, "readAt", LocalDateTime.now()));
    }

    /**
     * Lấy số lượng tin nhắn chưa đọc cho một session
     */
    public Integer getUnreadCount(String sessionId, String readerName) {
        ChatSession session = chatSessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chat session"));

        return chatMessageRepository.countUnreadMessagesForReader(session, readerName);
    }

    // Helper methods
    private SenderType determineSenderType(String senderName, ChatSession session) {
        if (session.getCustomerName().equals(senderName)) {
            return SenderType.CUSTOMER;
        }
        return SenderType.STAFF;
    }

    private void sendRealtimeMessage(ChatMessageDTO messageDTO) {
        // Send to specific session topic
        messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getSessionId(), messageDTO);
        
        // Send to staff dashboard
        messagingTemplate.convertAndSend("/topic/staff/messages", messageDTO);
    }

    private void notifyStaffNewSession(ChatSession session) {
        ChatSessionDTO sessionDTO = convertToSessionDTO(session);
        messagingTemplate.convertAndSend("/topic/staff/new-session", sessionDTO);
    }

    private ChatSessionDTO convertToSessionDTO(ChatSession session) {
        // Tính unread count cho staff (không đếm tin nhắn của chính staff)
        Integer unreadCount = 0;
        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.STAFF) {
                unreadCount = chatMessageRepository.countUnreadMessagesForReader(session, currentUser.getFullname());
            } else {
                // Fallback to old method for backward compatibility
                unreadCount = chatMessageRepository.countUnreadMessagesBySession(session);
            }
        } catch (Exception e) {
            // If no authenticated user, use old method
            unreadCount = chatMessageRepository.countUnreadMessagesBySession(session);
        }

        String lastMessage = chatMessageRepository.findLastMessageBySession(session)
                .map(ChatMessage::getMessage)
                .orElse(null);

        return ChatSessionDTO.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .customerName(session.getCustomerName())
                .staffName(session.getStaff() != null ? session.getStaff().getFullname() : null)
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getChatSession().getSessionId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .isRead(message.getIsRead())
                .build();
    }
}
