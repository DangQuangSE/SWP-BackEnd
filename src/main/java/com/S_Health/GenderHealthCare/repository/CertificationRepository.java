package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Certification;
import com.S_Health.GenderHealthCare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    
    List<Certification> findByConsultantAndIsActiveTrue(User consultant);
    
    List<Certification> findByConsultantIdAndIsActiveTrue(Long consultantId);
    
    Optional<Certification> findByIdAndConsultantAndIsActiveTrue(Long id, User consultant);
    
//    @Query("SELECT c FROM Certification c WHERE c.consultant.id = :consultantId AND c.isActive = true ORDER BY c.createdAt DESC")
//    List<Certification> findActiveByConsultantIdOrderByCreatedDesc(@Param("consultantId") Long consultantId);
//
}
