package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.repository.BlogRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BlogService {
    @Autowired
    BlogRepository blogRepository;

    @Transactional
    public Blog viewBlog(long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow(() -> new NoSuchElementException("Không tìm thấy bài viết"));
        blog.setViewCount(blog.getViewCount() + 1);
        return blog;
    }
    @Transactional
    public void likeBlog(long blogId) {
        Blog blog = blogRepository.findById(blogId).orElseThrow();
        blog.setLikeCount(blog.getLikeCount() + 1);
        blogRepository.save(blog);
    }
    public List<BlogSummaryDTO> getAllBlogSummaries() {
        return blogRepository.findAllBlogSummaries();
    }
}
