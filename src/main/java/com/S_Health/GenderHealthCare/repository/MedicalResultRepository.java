package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.MedicalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalResultRepository extends JpaRepository<MedicalResult, Long> {
    Optional<MedicalResult> findByIdAndIsActiveTrue(Long id);
    List<MedicalResult> findAllByAppointmentDetailIdAndIsActiveTrue(Long appointmentDetailId);

}
