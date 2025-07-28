package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ConfigValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigValueRepository extends JpaRepository<ConfigValue, Long> {

    Optional<ConfigValue> findByName(String name);
    boolean existsByName(String name);
}
