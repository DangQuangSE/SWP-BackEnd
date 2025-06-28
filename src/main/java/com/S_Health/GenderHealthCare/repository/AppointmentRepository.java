package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByMedicalProfileId(Long medicalProfileId);
    List<Appointment> findByMedicalProfileIdAndStatus(Long medicalProfileId, AppointmentStatus status);
    List<Appointment> findByPreferredDateAndConsultantAndStatusAndIsActiveTrue(LocalDate date, User consultant, AppointmentStatus status);
    List<Appointment> findByPreferredDateAndConsultantAndIsActiveTrue(LocalDate date, User consultant);
    List<Appointment> findByMedicalProfileAndStatusAndIsActiveTrue(MedicalProfile medicalProfile, AppointmentStatus status);
    List<Appointment> findByCustomerAndStatusAndIsActiveTrue(User customer, AppointmentStatus status);
    List<Appointment> findByConsultantAndStatusAndIsActiveTrue(User customer, AppointmentStatus status);
    List<Appointment> findByStatusAndIsActiveTrue(AppointmentStatus status);
}
