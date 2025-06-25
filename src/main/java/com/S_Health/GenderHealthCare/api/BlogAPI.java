package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.dto.response.BlogResponse;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
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
        Blog blog = blogService.viewBlog(id);
        return ResponseEntity.ok(blog);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Thích blog", description = "Thích blog và tăng lượt thích")
    public ResponseEntity<String> likeBlog(@PathVariable long id) {
        blogService.likeBlog(id);
        return ResponseEntity.ok("Đã thả tim cho bài viết!");
    }

    @GetMapping("/summary")
    @Operation(summary = "Lấy tóm tắt blog", description = "Lấy danh sách tóm tắt tất cả các blog đã xuất bản")
    public ResponseEntity getSummaryBlog(){
        return ResponseEntity.ok(blogService.getAllBlogSummaries());
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả blog", description = "Lấy tất cả blog đã xuất bản với phân trang")
    public ResponseEntity<Page<BlogResponse>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getAllBlogs(page, size));
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
    @Operation(summary = "Tạo blog mới", description = "Tạo blog mới với hình ảnh")
    public ResponseEntity createBlogWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("status") String status,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "tags", required = false) List<String> tags) {

        BlogRequest request = new BlogRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setImg(image);
        request.setTagNames(tags);
        try {
            request.setStatus(BlogStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái blog không hợp lệ");
        }

        return ResponseEntity.ok(blogService.createBlog(request));
    }
}
