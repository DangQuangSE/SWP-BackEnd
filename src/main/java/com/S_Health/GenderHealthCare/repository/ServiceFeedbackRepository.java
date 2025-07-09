package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ServiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceFeedbackRepository extends JpaRepository<ServiceFeedback, Long> {
    Optional<ServiceFeedback> findByAppointmentId(Long appointmentId);
}
