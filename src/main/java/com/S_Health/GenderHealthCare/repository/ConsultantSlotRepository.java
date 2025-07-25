package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ConsultantSlot;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultantSlotRepository extends JpaRepository<ConsultantSlot, Long> {
    List<ConsultantSlot> findByConsultantIdAndDateBetweenAndStatus(Long consultantId, LocalDate from, LocalDate to, SlotStatus status);
    Optional<ConsultantSlot> findByConsultantAndDateAndStartTimeAndStatus(User consultant, LocalDate date, LocalTime slot, SlotStatus status);
    List<ConsultantSlot> findByConsultantInAndDateBetweenAndStatus(List<User> consultants, LocalDate from, LocalDate to, SlotStatus status);
    ConsultantSlot findByConsultantAndDateAndStartTimeAndIsActiveTrue(User consultant, LocalDate date, LocalTime slotTime);
        List<ConsultantSlot> findByConsultantIdAndDate(Long consultantId, LocalDate date);
    @Query("""
        SELECT cs 
        FROM ConsultantSlot cs 
        WHERE cs.consultant.id = :consultantId
          AND cs.date = :date
          AND cs.startTime = :startTime
    """)
    Optional<ConsultantSlot> findByConsultantIdAndDateAndStartTime(Long consultantId, LocalDate date, LocalTime startTime);
}
