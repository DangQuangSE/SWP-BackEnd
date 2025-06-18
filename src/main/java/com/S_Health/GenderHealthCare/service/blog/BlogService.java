package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.dto.response.BlogResponse;
import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.BlogRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BlogService {
    @Autowired
    BlogRepository blogRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    ModelMapper modelMapper;
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

    @Transactional
    public BlogResponse createBlog(BlogRequest request) {
        request.validate();
        // 2. Lấy user từ JWT hoặc theo author_id
        User author = authenticationRepository.findById(request.getAuthor_id())
                .orElseThrow(() -> new AuthenticationException("Không tìm thấy người dùng này"));
        // 3. Tạo blog
        Blog blog = new Blog();
        blog.setTitle(request.getTitle().trim());
        blog.setContent(request.getContent().trim());
        blog.setImgUrl(request.getImgUrl());
        blog.setStatus(request.getStatus());
        blog.setAuthor(author);
        blogRepository.save(blog);
        // 4. Lưu vào DB
        BlogResponse blogResponse = modelMapper.map(blog, BlogResponse.class);
        return blogResponse;
    }
}
