package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.dto.response.BlogResponse;
import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.Tag;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.BlogRepository;
import com.S_Health.GenderHealthCare.service.cloudinary.CloudinaryService;
import com.S_Health.GenderHealthCare.service.tag.TagService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlogService {
    @Autowired
    BlogRepository blogRepository;
    @Autowired
    AuthenticationRepository authenticationRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    TagService tagService;

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
        User author = authUtil.getCurrentUser();
        // Upload image to Cloudinary if provided
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(request.getImg());
                request.setImgUrl(imageUrl);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        // 3. Tạo blog
        Blog blog = new Blog();
        blog.setTitle(request.getTitle().trim());
        blog.setContent(request.getContent().trim());
        blog.setImgUrl(request.getImgUrl());
        blog.setStatus(request.getStatus());
        blog.setAuthor(author);

        // Process tags if provided
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            List<Tag> tags = request.getTagNames().stream()
                .map(tagName -> tagService.getOrCreateTag(tagName))
                .collect(Collectors.toList());
            blog.setTags(tags);
        }

        blogRepository.save(blog);
        // 4. Lưu vào DB
        BlogResponse blogResponse = modelMapper.map(blog, BlogResponse.class);
        blogResponse.setAuthor(modelMapper.map(author, UserDTO.class));
        return blogResponse;
    }
    public Page<BlogResponse> getAllBlogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Blog> blogs = blogRepository.findAllPublishedBlogs(pageable);

        return blogs.map(blog -> {
            BlogResponse response = modelMapper.map(blog, BlogResponse.class);
            if (blog.getAuthor() != null) {
                response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
            }
            return response;
        });
    }

    public Page<BlogResponse> getBlogsByTag(Long tagId, int page, int size) {

        tagService.getTagById(tagId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Blog> blogs = blogRepository.findByTagId(tagId, pageable);

        return blogs.map(blog -> {
            BlogResponse response = modelMapper.map(blog, BlogResponse.class);
            if (blog.getAuthor() != null) {
                response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
            }
            return response;
        });
    }
}
