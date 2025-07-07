package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.dto.response.report.BookingReportResponse;
import com.S_Health.GenderHealthCare.dto.response.report.ServiceBookingReportDTO;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.entity.Payment;
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
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByMedicalProfileId(Long medicalProfileId);
    List<Appointment> findByMedicalProfileIdAndStatus(Long medicalProfileId, AppointmentStatus status);
    List<Appointment> findByPreferredDateAndConsultantAndStatusAndIsActiveTrue(LocalDate date, User consultant, AppointmentStatus status);
    List<Appointment> findByPreferredDateAndConsultantAndIsActiveTrue(LocalDate date, User consultant);
    List<Appointment> findByMedicalProfileAndStatusAndIsActiveTrue(MedicalProfile medicalProfile, AppointmentStatus status);
    List<Appointment> findByCustomerAndStatusAndIsActiveTrue(User customer, AppointmentStatus status);
    List<Appointment> findByConsultantAndStatusAndIsActiveTrue(User customer, AppointmentStatus status);
    List<Appointment> findByStatusAndIsActiveTrue(AppointmentStatus status);

    @Query("""
    SELECT new com.S_Health.GenderHealthCare.dto.response.report.ServiceBookingReportDTO(
        a.service.id,
        a.service.name,
        SUM(CASE WHEN a.status IN ('PENDING','CONFIRMED','PROCESSING','COMPLETED','CHECKED','ABSENT') THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status IN ('CANCELED','DELETED') THEN 1 ELSE 0 END)
    )
    FROM Appointment a
    WHERE a.created_at BETWEEN :startDate AND :endDate
      AND (:serviceId IS NULL OR a.service.id = :serviceId)
      AND a.isActive = true
    GROUP BY a.service.id, a.service.name
""")
    List<ServiceBookingReportDTO> getServiceBookingReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("serviceId") Long serviceId
    );


    @Query("""
    SELECT new com.S_Health.GenderHealthCare.dto.response.report.BookingReportResponse(
        SUM(CASE WHEN a.status IN ('PENDING','CONFIRMED','PROCESSING','COMPLETED','CHECKED') THEN 1 ELSE 0 END),
        SUM(CASE WHEN a.status IN ('CANCELED','DELETED') THEN 1 ELSE 0 END)
    )
    FROM Appointment a
    WHERE a.created_at BETWEEN :startDate AND :endDate
      AND a.isActive = true
""")
    BookingReportResponse getBookingSummary(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


}
