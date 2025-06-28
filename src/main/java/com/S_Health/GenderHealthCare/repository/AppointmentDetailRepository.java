package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, Long> {
    boolean existsByAppointment_Customer_IdAndSlotTime(Long customerId, LocalDateTime slotTime );
    List<AppointmentDetail> findByConsultant_idAndSlotTime(Long customer_id, LocalDateTime slotTime);
    List<AppointmentDetail> findByAppointment(Appointment appointment);
    List<AppointmentDetail> findByAppointmentAndIsActiveTrue(Appointment appointment);
    Optional<AppointmentDetail> findByAppointmentId(Long appointmentId);
}
