package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.HospitalSlotFree;
import com.S_Health.GenderHealthCare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HospitalSlotFreeRepository extends JpaRepository<HospitalSlotFree, Long> {
    List<HospitalSlotFree> findByDateAndSpecialization(LocalDate date, Specialization specialization);
}
