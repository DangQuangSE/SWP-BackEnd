package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Long> {
}
