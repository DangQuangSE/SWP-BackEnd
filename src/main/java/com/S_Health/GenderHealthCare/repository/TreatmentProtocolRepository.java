package com.S_Health.GenderHealthCare.repository;


import com.S_Health.GenderHealthCare.entity.TreatmentProtocol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreatmentProtocolRepository extends JpaRepository<TreatmentProtocol, Long> {
    List<TreatmentProtocol> findByDiseaseNameContainingIgnoreCase(String diseaseName);
    Optional<TreatmentProtocol> findByIdAndActiveTrue(Long id);
    List<TreatmentProtocol> findByActiveTrue();

}
