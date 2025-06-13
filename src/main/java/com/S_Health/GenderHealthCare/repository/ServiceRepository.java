package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<MedicalService, Long> {
    List<MedicalService> findByNameContainingIgnoreCase(String name);
    // Define any additional query methods if needed
    //Service findById(long id);
}
