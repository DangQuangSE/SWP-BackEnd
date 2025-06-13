package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
