package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.MedicalProfile;
import com.S_Health.GenderHealthCare.entity.Service;
import com.S_Health.GenderHealthCare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, Long> {
    Optional<MedicalProfile> findByCustomerAndIsActiveTrue(User currentUser);
    Optional<MedicalProfile> findByCustomerAndServiceAndIsActiveTrue(User currentUser, Service service);
}
