package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByMedicalProfileId(Long medicalProfileId);
    Optional<Appointment> findByIdAndStatus(Long appointmentId, AppointmentStatus status);
}
