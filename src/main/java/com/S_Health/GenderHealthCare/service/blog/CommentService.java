package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.request.blog.CommentRequest;
import com.S_Health.GenderHealthCare.dto.response.CommentResponse;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.Comment;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AuthenticationRepository;
import com.S_Health.GenderHealthCare.repository.BlogRepository;
import com.S_Health.GenderHealthCare.repository.CommentRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private AuthenticationRepository authenticationRepository;
    @Autowired
    private AuthUtil authUtil;

    public CommentResponse createComment(CommentRequest request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new AppException("Không tim thấy blog hoặc không có blog"));

        Long userId = authUtil.getCurrentUserId();
        User commenter = authenticationRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));

        Comment comment = Comment.builder()
                .blog(blog)
                .commenter(commenter)
                .description(request.getDescription())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Convert to CommentResponse
        return new CommentResponse(
                savedComment.getId(),
                savedComment.getCommenter().getFullname() != null ? savedComment.getCommenter().getFullname() : "Unknown User",
                savedComment.getDescription(),
                savedComment.getCreateAt()
        );
    }

    public List<CommentResponse> getCommentsByBlog(Long blogId) {
        List<Comment> comments = commentRepository.findByBlogId(blogId);

        return comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getCommenter().getFullname() != null ? comment.getCommenter().getFullname() : "Unknown User",
                        comment.getDescription(),
                        comment.getCreateAt()
                ))
                .toList();
    }

    public void deleteComment(Long commentID) {
        User user = authUtil.getCurrentUser();
        Comment comment = commentRepository.findById(commentID).orElseThrow(() -> new AppException("Không tìm thấy bình luận"));
        if (comment.getCommenter().getId() != user.getId()) {
            throw new AppException("Bạn không có quyền xóa bình luận này");
        }
        commentRepository.delete(comment);
    }
}
