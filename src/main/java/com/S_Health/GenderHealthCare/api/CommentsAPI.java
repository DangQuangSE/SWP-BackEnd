package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.blog.CommentRequest;
import com.S_Health.GenderHealthCare.entity.Comment;
import com.S_Health.GenderHealthCare.service.blog.CommentService;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentsAPI {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody CommentRequest request) throws NotFoundException {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    @GetMapping("/blog/{blogId}")
    public ResponseEntity<List<Comment>> getCommentsByBlog(@PathVariable Long blogId) {
        return ResponseEntity.ok(commentService.getCommentsByBlog(blogId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
