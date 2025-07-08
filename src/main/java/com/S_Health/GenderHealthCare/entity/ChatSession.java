package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.ChatStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    String sessionId;
    
    @Column(name = "customer_name", nullable = false)
    String customerName;
    
    @ManyToOne
    @JoinColumn(name = "staff_id")
    User staff; // Staff đang handle session này
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    ChatStatus status = ChatStatus.WAITING;

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "ended_at")
    LocalDateTime endedAt;
    
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<ChatMessage> messages;
    
    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;
}
