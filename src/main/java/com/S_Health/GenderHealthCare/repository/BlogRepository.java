package com.S_Health.GenderHealthCare.repository;

import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("SELECT new com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO(b.id, b.title, b.viewCount, b.likeCount, COUNT(c.id)) " +
            "FROM Blog b LEFT JOIN Comment c ON b.id = c.blog.id " +
            "WHERE b.status = 'PUBLISHED' " +
            "GROUP BY b.id " +
            "ORDER BY b.createdAt DESC")
    List<BlogSummaryDTO> findAllBlogSummaries();

    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY b.createdAt DESC")
    Page<Blog> findAllPublishedBlogs(Pageable pageable);

    @Query("SELECT DISTINCT b FROM Blog b JOIN b.tags t WHERE b.status = 'PUBLISHED' AND t.id = :tagId ORDER BY b.createdAt DESC")
    Page<Blog> findByTagId(Long tagId, Pageable pageable);

    Page<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status, Pageable pageable);
    
    Page<Blog> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    
    // Thêm các method mới
    Page<Blog> findByAuthorAndStatusOrderByCreatedAtDesc(User author, BlogStatus status, Pageable pageable);
}