package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.blog.BlogRequest;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.service.blog.BlogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
    
@RestController
@SecurityRequirement(name = "api/blog")
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

    @PostMapping("/blog")
    public ResponseEntity createBlog(@RequestBody BlogRequest request) {
        return ResponseEntity.ok(blogService.createBlog(request));
    }
}
