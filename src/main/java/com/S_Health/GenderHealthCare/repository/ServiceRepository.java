package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByNameContainingIgnoreCase(String name);
    // Define any additional query methods if needed
    //Service findById(long id);
}
