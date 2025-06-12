package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ServiceSlotPool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ServiceSlotPoolRepository extends JpaRepository<ServiceSlotPool, Long> {
    Optional<ServiceSlotPool> findByService_idAndDateAndStartTime(Long serviceId, LocalDate date, LocalTime startTime);
}
