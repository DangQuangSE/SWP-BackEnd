package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Payment;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointmentIdAndStatus(Long appointmentId, PaymentStatus status);
    Optional<Payment> findByAppointmentId(Long appointmentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM Payment p " +
            "WHERE p.status <> :status")
    BigDecimal getTotalRevenueExcludeStatus(@Param("status") PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status <> :status
          AND DATE(p.createdAt) = CURRENT_DATE
        """)
    BigDecimal getTodayRevenue(@Param("status") PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status <> :status
          AND YEAR(p.createdAt) = YEAR(CURRENT_DATE)
          AND MONTH(p.createdAt) = MONTH(CURRENT_DATE)
        """)
    BigDecimal getCurrentMonthRevenue(@Param("status") PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status <> :status
          AND YEAR(p.createdAt) = YEAR(CURRENT_DATE)
        """)
    BigDecimal getCurrentYearRevenue(@Param("status") PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status <> :status
          AND p.createdAt BETWEEN :startDate AND :endDate
        """)
    BigDecimal getRevenueByDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
