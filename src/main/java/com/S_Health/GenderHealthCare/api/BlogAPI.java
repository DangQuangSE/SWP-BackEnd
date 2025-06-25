package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
import com.S_Health.GenderHealthCare.service.blog.BlogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
    
@RestController
@RequestMapping("/api/blog")
@SecurityRequirement(name = "api")
public class BlogAPI {
    @Autowired
    BlogService blogService;
    @GetMapping("/{id}")
    public ResponseEntity getBlogAndIncreaseView(@PathVariable long id) {
        Blog blog = blogService.viewBlog(id);
        return ResponseEntity.ok(blog);
    }
    @PostMapping("/{id}/like")
    public ResponseEntity<String> likeBlog(@PathVariable long id) {
        blogService.likeBlog(id);
        return ResponseEntity.ok("Đã thả tim cho bài viết!");
    }
    @GetMapping("/summary")
    public ResponseEntity getSummaryBlog(){
        return ResponseEntity.ok(blogService.getAllBlogSummaries());
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity createBlogWithImage(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("status") String status,
            @RequestParam("image") MultipartFile image) {

        BlogRequest request = new BlogRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setImg(image);
        try {
            request.setStatus(BlogStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái blog không hợp lệ");
        }

        return ResponseEntity.ok(blogService.createBlog(request));
    }
}
