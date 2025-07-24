package com.S_Health.GenderHealthCare.dto.request.blog;

import com.S_Health.GenderHealthCare.enums.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private BlogStatus status; // Chỉ dùng khi tạo blog mới, không được cập nhật trong updateBlog

    MultipartFile img;
    String imgUrl;

    List<String> tagNames;

    public void validate() {
        if (title == null || title.trim().length() < 10) {
            throw new IllegalArgumentException("Tiêu đề phải có ít nhất 10 ký tự.");
        }

        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("Nội dung bài viết phải có ít nhất 50 ký tự.");
        }

    }
}
