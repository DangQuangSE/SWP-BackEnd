package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    List<Specialization> findByServiceId(long service_id);
}
