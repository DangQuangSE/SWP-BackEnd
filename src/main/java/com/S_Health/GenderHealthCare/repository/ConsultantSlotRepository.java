package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ConsultantSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConsultantSlotRepository extends JpaRepository<ConsultantSlot, Long> {
    List<ConsultantSlot> findByConsultantIdAndDateBetween(Long consultantId, LocalDate from, LocalDate to);

}
