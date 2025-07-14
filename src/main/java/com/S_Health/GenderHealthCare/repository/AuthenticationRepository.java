package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthenticationRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    User findUserByEmail(String email);
    List<User> findBySpecializations_IdInAndIsActive(List<Long> specializations, boolean isActive);
    List<User> findByRole(UserRole role);
}
