package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.dto.response.BlogResponse;
import com.S_Health.GenderHealthCare.dto.response.BlogSummaryDTO;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.Tag;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
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

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

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
                throw new AppException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        // 3. Tạo blog
        Blog blog = new Blog();
        blog.setTitle(request.getTitle().trim());
        blog.setContent(request.getContent().trim());
        blog.setImgUrl(request.getImgUrl());
        // Mặc định là DRAFT, author sẽ phải submit để duyệt
        if (request.getStatus() == BlogStatus.PUBLISHED) {
            // Chỉ admin mới có thể tạo blog với status PUBLISHED
            if (author.getRole() == UserRole.ADMIN) {
                blog.setStatus(BlogStatus.PUBLISHED);
            } else {
                blog.setStatus(BlogStatus.DRAFT);
            }
        } else {
            blog.setStatus(request.getStatus());
        }

        blog.setAuthor(author);

        // Process tags if provided - SỬ DỤNG METHOD MỚI
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            // Kiểm tra tag không hợp lệ
            List<String> invalidTags = tagService.validateTagNames(request.getTagNames());
            if (!invalidTags.isEmpty()) {
                throw new AppException("Các tag sau không tồn tại: " + String.join(", ", invalidTags));
            }
            
            // Lấy các tag đã tồn tại
            List<Tag> tags = tagService.getExistingTags(request.getTagNames());
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
    @Transactional
    public BlogResponse updateBlog(Long blogId, BlogRequest request) {
        request.validate();
        
        // Tìm blog
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        // Kiểm tra quyền sở hữu
        User currentUser = authUtil.getCurrentUser();
        if (!(blog.getAuthor().getId() == (currentUser.getId()))) {
            throw new AppException("Bạn không có quyền chỉnh sửa bài viết này");
        }
        
        // Upload hình ảnh mới nếu có
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(request.getImg());
                blog.setImgUrl(imageUrl);
            } catch (IOException e) {
                throw new AppException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        } else if (request.getImgUrl() != null) {
            blog.setImgUrl(request.getImgUrl());
        }
        
        // Cập nhật thông tin blog
        blog.setTitle(request.getTitle().trim());
        blog.setContent(request.getContent().trim());
        blog.setStatus(request.getStatus());
        
        // Cập nhật tags nếu có - SỬ DỤNG METHOD MỚI
        if (request.getTagNames() != null) {
            // Kiểm tra tag không hợp lệ
            List<String> invalidTags = tagService.validateTagNames(request.getTagNames());
            if (!invalidTags.isEmpty()) {
                throw new AppException("Các tag sau không tồn tại: " + String.join(", ", invalidTags));
            }
            
            // Lấy các tag đã tồn tại
            List<Tag> tags = tagService.getExistingTags(request.getTagNames());
            blog.setTags(tags);
        }
        
        blogRepository.save(blog);
        
        BlogResponse blogResponse = modelMapper.map(blog, BlogResponse.class);
        blogResponse.setAuthor(modelMapper.map(currentUser, UserDTO.class));
        return blogResponse;
    }

    @Transactional
    public void deleteBlog(Long blogId) {
        // Tìm blog
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        // Kiểm tra quyền sở hữu
        User currentUser = authUtil.getCurrentUser();
        if (!(blog.getAuthor().getId() == (currentUser.getId()))) {
            throw new AppException("Bạn không có quyền xóa bài viết này");
        }
        
        // Xóa blog
        blogRepository.delete(blog);
    }

    public Page<BlogResponse> getMyBlogs(int page, int size) {
        User currentUser = authUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Thêm method mới vào BlogRepository
        Page<Blog> blogs = blogRepository.findByAuthorOrderByCreatedAtDesc(currentUser, pageable);
        
        return blogs.map(blog -> {
            BlogResponse response = modelMapper.map(blog, BlogResponse.class);
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
            return response;
        });
    }

    public BlogResponse getBlogById(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        if (blog.getAuthor() != null) {
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
        }
        return response;
    }

    public Page<BlogResponse> getMyBlogsByStatus(BlogStatus status, int page, int size) {
        User currentUser = authUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Thêm method mới vào BlogRepository
        Page<Blog> blogs = blogRepository.findByAuthorAndStatusOrderByCreatedAtDesc(currentUser, status, pageable);

        return blogs.map(blog -> {
            BlogResponse response = modelMapper.map(blog, BlogResponse.class);
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
            return response;
        });
    }

    public Page<BlogResponse> getBlogsByStatus(BlogStatus status, int page, int size) {
        // Chỉ admin/staff mới có thể xem blog theo status
        User currentUser = authUtil.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new AppException("Bạn không có quyền xem danh sách blog theo trạng thái");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Blog> blogs = blogRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        
        return blogs.map(blog -> {
            BlogResponse response = modelMapper.map(blog, BlogResponse.class);
            if (blog.getAuthor() != null) {
                response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
            }
            return response;
        });
    }

    @Transactional
    public BlogResponse approveBlog(Long blogId) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.STAFF) {
            throw new AppException("Bạn không có quyền duyệt bài viết");
        }
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new AppException("Chỉ có thể duyệt bài viết có trạng thái PENDING");
        }
        
        blog.setStatus(BlogStatus.APPROVED);
        blogRepository.save(blog);
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        if (blog.getAuthor() != null) {
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
        }
        return response;
    }

    @Transactional
    public BlogResponse rejectBlog(Long blogId) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.STAFF) {
            throw new AppException("Bạn không có quyền từ chối bài viết");
        }
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        if (blog.getStatus() != BlogStatus.PENDING) {
            throw new AppException("Chỉ có thể từ chối bài viết có trạng thái PENDING");
        }
        
        blog.setStatus(BlogStatus.REJECTED);
        blogRepository.save(blog);
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        if (blog.getAuthor() != null) {
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
        }
        return response;
    }

    @Transactional
    public BlogResponse publishBlog(Long blogId) {
        User currentUser = authUtil.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN && currentUser.getRole() != UserRole.STAFF) {
            throw new AppException("Bạn không có quyền đăng bài viết");
        }
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        if (blog.getStatus() != BlogStatus.APPROVED) {
            throw new AppException("Chỉ có thể đăng bài viết đã được duyệt");
        }
        
        blog.setStatus(BlogStatus.PUBLISHED);
        blogRepository.save(blog);
        
        log.info("Blog {} đã được đăng bởi admin {}", blogId, currentUser.getId());
        
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        if (blog.getAuthor() != null) {
            response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
        }
        return response;
    }

    // Thêm method submit blog cho author
    @Transactional
    public BlogResponse submitBlogForReview(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException("Không tìm thấy bài viết"));
        
        User currentUser = authUtil.getCurrentUser();
        if (!(blog.getAuthor().getId() == (currentUser.getId()))) {
            throw new AppException("Bạn không có quyền gửi bài viết này để duyệt");
        }
        
        if (blog.getStatus() != BlogStatus.DRAFT && blog.getStatus() != BlogStatus.REJECTED) {
            throw new AppException("Chỉ có thể gửi bài viết ở trạng thái DRAFT hoặc REJECTED để duyệt");
        }
        
        blog.setStatus(BlogStatus.PENDING);
        blogRepository.save(blog);
        
        log.info("Blog {} đã được gửi để duyệt bởi author {}", blogId, currentUser.getId());
        
        BlogResponse response = modelMapper.map(blog, BlogResponse.class);
        response.setAuthor(modelMapper.map(blog.getAuthor(), UserDTO.class));
        return response;
    }
}