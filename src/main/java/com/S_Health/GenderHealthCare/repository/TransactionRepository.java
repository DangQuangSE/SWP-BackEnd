package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByOrderId(String orderId);

}
