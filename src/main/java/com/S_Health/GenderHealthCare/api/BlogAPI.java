package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.dto.response.BlogResponse;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.service.blog.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/blog")
@SecurityRequirement(name = "api")
public class BlogAPI {
    @Autowired
    BlogService blogService;

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết blog", description = "Xem chi tiết blog và tăng lượt xem")
    public ResponseEntity getBlogAndIncreaseView(@PathVariable long id) {
        return ResponseEntity.ok(blogService.viewBlog(id));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Thích blog", description = "Thích blog và tăng lượt thích")
    public ResponseEntity<String> likeBlog(@PathVariable long id) {
        blogService.likeBlog(id);
        return ResponseEntity.ok("Đã thả tim cho bài viết!");
    }

    @GetMapping("/summary")
    @Operation(summary = "Lấy tóm tắt blog", description = "Lấy danh sách tóm tắt tất cả các blog đã xuất bản")
    public ResponseEntity getSummaryBlog() {
        return ResponseEntity.ok(blogService.getAllBlogSummaries());
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả blog", description = "Lấy tất cả blog đã xuất bản với phân trang")
    public ResponseEntity<Page<BlogResponse>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getAllBlogs(page, size));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Lấy tất cả blog cho quản lý",
               description = "Admin xem tất cả blog (bao gồm cả chưa publish)")
    public ResponseEntity<Page<BlogResponse>> getAllBlogsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getAllBlogsForManagement(page, size));
    }

    @GetMapping("/by-tag/{tagId}")
    @Operation(summary = "Lấy blog theo tag", description = "Lấy blog theo tag với phân trang")
    public ResponseEntity<Page<BlogResponse>> getBlogsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getBlogsByTag(tagId, page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo blog mới", description = "Tạo blog mới với hình ảnh. Status sẽ được tự động set: PENDING cho user thường, PUBLISHED cho Admin")
    public ResponseEntity createBlogWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "tags", required = false) List<String> tags) {

        BlogRequest request = new BlogRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setImg(image);
        request.setTagNames(tags);  
        // Không cần set status nữa, sẽ được xử lý tự động trong service
        return ResponseEntity.ok(blogService.createBlog(request));
    }

    // API mới cho tác giả
    @GetMapping("/my-blogs")
    @Operation(summary = "Lấy blog của tôi", description = "Lấy tất cả blog của tác giả hiện tại")
    public ResponseEntity<Page<BlogResponse>> getMyBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getMyBlogs(page, size));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Lấy chi tiết blog", description = "Lấy chi tiết blog không tăng view count")
    public ResponseEntity<BlogResponse> getBlogDetail(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật blog", description = "Cập nhật blog của tác giả")
    public ResponseEntity updateBlog(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "tags", required = false) List<String> tags) {

        BlogRequest request = new BlogRequest();
        request.setTitle(title);
        request.setContent(content);
        if (image != null && !image.isEmpty()) {
            request.setImg(image);
        }
        request.setTagNames(tags);
        try {
            BlogResponse response = blogService.updateBlog(id, request);
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa blog", description = "Xóa blog của tác giả")
    public ResponseEntity<String> deleteBlog(@PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.ok("Đã xóa bài viết thành công");
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API cho admin xem blog theo status
    @GetMapping("/admin/by-status")
    @Operation(summary = "Lấy blog theo trạng thái", description = "Admin xem blog theo trạng thái cụ thể")
    public ResponseEntity<Page<BlogResponse>> getBlogsByStatus(
            @RequestParam BlogStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getBlogsByStatus(status, page, size));
    }

    // API admin duyệt blog
    @PostMapping("/admin/{id}/approve")
    @Operation(summary = "Duyệt blog", description = "Admin duyệt blog")
    public ResponseEntity<BlogResponse> approveBlog(
            @PathVariable Long id) {
        return ResponseEntity.ok(blogService.approveBlog(id));
    }

    // API admin từ chối blog
    @PostMapping("/admin/{id}/reject")
    @Operation(summary = "Từ chối blog", description = "Admin từ chối blog")
    public ResponseEntity<BlogResponse> rejectBlog(
            @PathVariable Long id) {
        return ResponseEntity.ok(blogService.rejectBlog(id));
    }

    // API admin đăng blog
    @PostMapping("/admin/{id}/publish")
    @Operation(summary = "Đăng blog", description = "Admin đăng blog đã duyệt")
    public ResponseEntity<BlogResponse> publishBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.publishBlog(id));
    }

    // API author gửi blog để duyệt
    @PostMapping("/{id}/submit")
    @Operation(summary = "Gửi blog để duyệt", description = "Author gửi blog để admin duyệt")
    public ResponseEntity<BlogResponse> submitBlogForReview(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.submitBlogForReview(id));
    }

    // API author xem blog của mình theo status
    @GetMapping("/my-blogs/by-status")
    @Operation(summary = "Lấy blog của tôi theo trạng thái", description = "Author xem blog của mình theo trạng thái")
    public ResponseEntity<Page<BlogResponse>> getMyBlogsByStatus(
            @RequestParam BlogStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getMyBlogsByStatus(status, page, size));
    }
}