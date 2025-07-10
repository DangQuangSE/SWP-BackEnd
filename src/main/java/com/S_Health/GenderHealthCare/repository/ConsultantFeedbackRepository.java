package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ConsultantFeedback;
import com.S_Health.GenderHealthCare.entity.ServiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultantFeedbackRepository extends JpaRepository<ConsultantFeedback, Long> {

    Optional<ConsultantFeedback> findByServiceFeedbackId(Long feedbackId);
    List<ConsultantFeedback> findByServiceFeedbackIdIn(List<Long> serviceFeedbackIds);
}
