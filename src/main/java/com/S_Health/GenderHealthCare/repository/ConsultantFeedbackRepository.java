package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ConsultantFeedback;
import com.S_Health.GenderHealthCare.entity.ServiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultantFeedbackRepository extends JpaRepository<ConsultantFeedback, Long> {

    List<ServiceFeedback> findByServiceFeedbackId(Long feedbackId);
}
