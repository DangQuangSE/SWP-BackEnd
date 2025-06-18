package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.CycleTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CycleTrackingRepository extends JpaRepository<CycleTracking, Long> {
    Optional<CycleTracking> findByUser_IdAndStartDate(Long userId, LocalDate startDate);
    List<CycleTracking> findAllByUserId(Long userId);
}
