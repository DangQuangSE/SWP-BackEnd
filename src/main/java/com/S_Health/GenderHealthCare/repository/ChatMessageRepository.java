package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ChatMessage;
import com.S_Health.GenderHealthCare.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByChatSessionOrderBySentAtAsc(ChatSession chatSession);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession = :session AND cm.isRead = false")
    Integer countUnreadMessagesBySession(@Param("session") ChatSession session);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :session ORDER BY cm.sentAt DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageBySession(@Param("session") ChatSession session);

    List<ChatMessage> findByChatSessionAndIsReadFalse(ChatSession chatSession);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession = :session AND cm.isRead = false AND cm.senderName != :readerName")
    Integer countUnreadMessagesForReader(@Param("session") ChatSession session, @Param("readerName") String readerName);
}
