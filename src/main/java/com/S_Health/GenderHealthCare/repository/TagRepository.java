package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameAndIsActiveTrue(String name);
    boolean existsByName(String name);
}
