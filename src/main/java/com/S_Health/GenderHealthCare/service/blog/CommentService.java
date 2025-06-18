package com.S_Health.GenderHealthCare.service.blog;

import com.S_Health.GenderHealthCare.dto.request.blog.CommentRequest;
import com.S_Health.GenderHealthCare.entity.Blog;
import com.S_Health.GenderHealthCare.entity.Comment;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.repository.BlogRepository;
import com.S_Health.GenderHealthCare.repository.CommentRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private AuthUtil authUtil;

    public Comment createComment(CommentRequest request) throws NotFoundException {
        Blog blog = blogRepository.findById(request.getBogId())
                .orElseThrow(()-> new NotFoundException("Không tim thấy blog hoặc không có blog"));

        Long userId = authUtil.getCurrentUserId();
        User commenter = new User();
        commenter.setId(userId);

        Comment comment = Comment.builder()
                .blog(blog)
                .commenter(commenter)
                .description(request.getDescription())
                .build();

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByBlog(Long blogId){
        return commentRepository.findByBlogIdAndIsDeletedFalse(blogId);
    }

//    public void deleteComment(Long commentID){
//        Long userId = authUtil.getCurrentUserId();;
//
//        Comment comment = commentRepository.findById(commentID)
//                .orElseThrow(()-> new ResourceNotFoundException("Không tìm thấy bình luận"))
//    }
}
