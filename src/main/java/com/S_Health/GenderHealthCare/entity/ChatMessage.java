package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne
    @JoinColumn(name = "chat_session_id", nullable = false)
    ChatSession chatSession;
    
    @Column(name = "sender_name", nullable = false)
    String senderName;
    
    @Column(name = "sender_type", nullable = false)
    @Enumerated(EnumType.STRING)
    SenderType senderType;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    String message;
    
    @Column(name = "sent_at")
    @Builder.Default
    LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "is_read")
    @Builder.Default
    Boolean isRead = false;
}
