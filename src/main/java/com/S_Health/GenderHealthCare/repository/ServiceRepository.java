package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByIsActiveTrue();
    List<Service> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    List<Service> findBySpecializationsContainingAndIsActiveTrue(Specialization specialization);
    boolean existsByNameAndIsActiveTrue(String name);
}
