package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Specialization;
import com.S_Health.GenderHealthCare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    List<Specialization> findByIsActiveTrue();
    boolean existsByNameAndIsActiveTrue(String name);
    List<Specialization> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    List<Specialization> findByServicesIdAndIsActiveTrue(Long serviceId);
}
