package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ChatSession;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionIdAndIsActiveTrue(String sessionId);

    List<ChatSession> findByStatusAndIsActiveTrueOrderByUpdatedAtDesc(ChatStatus status);

    List<ChatSession> findByStaffAndIsActiveTrueOrderByUpdatedAtDesc(User staff);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.isActive = true ORDER BY cs.updatedAt DESC")
    List<ChatSession> findAllActiveSessionsOrderByUpdatedAtDesc();

    @Query("SELECT cs FROM ChatSession cs WHERE cs.status = :status AND cs.isActive = true ORDER BY cs.createdAt ASC")
    List<ChatSession> findWaitingSessionsOrderByCreatedAt(@Param("status") ChatStatus status);

    @Query("SELECT cs FROM ChatSession cs WHERE cs.status IN :statuses AND cs.isActive = true ORDER BY cs.updatedAt DESC")
    List<ChatSession> findByStatusInAndIsActiveTrueOrderByUpdatedAtDesc(@Param("statuses") List<ChatStatus> statuses);
}
