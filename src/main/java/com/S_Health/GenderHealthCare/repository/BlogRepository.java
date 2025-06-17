package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("SELECT new com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO(b.id, b.title, b.viewCount, b.likeCount, COUNT(c.id)) " +
            "FROM Blog b LEFT JOIN Comment c ON b.id = c.blog.id " +
            "WHERE b.status = 'PUBLISHED' " +
            "GROUP BY b.id " +
            "ORDER BY b.createdAt DESC")
    List<BlogSummaryDTO> findAllBlogSummaries();
}
