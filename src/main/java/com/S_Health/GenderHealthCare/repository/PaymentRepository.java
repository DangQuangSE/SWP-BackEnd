package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentIdAndStatus(Long appointmentId, PaymentStatus status);
    Optional<Payment> findByAppointmentId(Long appointmentId);
}
