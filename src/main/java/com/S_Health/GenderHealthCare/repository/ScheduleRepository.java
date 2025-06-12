package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByConsultantIdAndWorkDateBetween(Long consultantId, LocalDate from, LocalDate to);
    List<Schedule> findByConsultantIdInAndWorkDateBetween(List<Long> consultantIds, LocalDate from, LocalDate to);

}
