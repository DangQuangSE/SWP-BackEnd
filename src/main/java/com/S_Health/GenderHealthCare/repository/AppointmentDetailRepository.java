package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, Long> {
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppointmentDetail a WHERE a.appointment.customer.id = :customerId AND a.slotTime = :slotTime AND a.status != 'CANCELED' AND a.isActive = true")
    boolean existsByAppointment_Customer_IdAndSlotTime(@Param("customerId") Long customerId, @Param("slotTime") LocalDateTime slotTime);
    List<AppointmentDetail> findByConsultant_idAndSlotTime(Long customer_id, LocalDateTime slotTime);
    List<AppointmentDetail> findByAppointment(Appointment appointment);
    List<AppointmentDetail> findByAppointmentAndIsActiveTrue(Appointment appointment);
    Optional<AppointmentDetail> findByAppointmentId(Long appointmentId);
    @Query("SELECT a FROM AppointmentDetail a WHERE a.consultant.id = :consultantId AND DATE(a.slotTime) = :date AND a.isActive = true")
    List<AppointmentDetail> findByConsultant_idAndSlotDate(@Param("consultantId") Long consultantId,
                                                           @Param("date") LocalDate date);

    @Query("SELECT a FROM AppointmentDetail a WHERE a.consultant.id = :consultantId AND DATE(a.slotTime) = :date AND a.status = :status AND a.isActive = true")
    List<AppointmentDetail> findByConsultant_idAndSlotDateAndStatus(@Param("consultantId") Long consultantId,
                                                                   @Param("date") LocalDate date,
                                                                   @Param("status") AppointmentStatus status);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppointmentDetail a WHERE a.consultant.id = :consultantId AND a.appointment.customer.id = :customerId AND a.isActive = true")
    boolean existsByConsultantIdAndAppointmentCustomerId(@Param("consultantId") Long consultantId,
                                                        @Param("customerId") Long customerId);
}
