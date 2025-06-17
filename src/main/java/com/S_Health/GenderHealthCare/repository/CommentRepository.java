package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
