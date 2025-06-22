package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.MedicalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalResultRepository extends JpaRepository<MedicalResult, Long> {
}
