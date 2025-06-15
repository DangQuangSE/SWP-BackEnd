package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, Long> {

    @Query("SELECT COUNT(a) FROM AppointmentDetail a WHERE a.consultant.id = :consultantId AND a.slotTime = :slotTime")
    int countByConsultant_idAndSlotTime(@Param("consultantId") Long consultantId, @Param("slotTime") LocalDateTime slotTime);
    boolean existsByAppointment_Customer_IdAndSlotTime(Long customerId, LocalDateTime slotTime );

}
