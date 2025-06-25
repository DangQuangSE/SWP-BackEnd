package com.S_Health.GenderHealthCare.dto.request.blog;

import com.S_Health.GenderHealthCare.enums.BlogStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogRequest {
    String title;
    String content;
    MultipartFile img;
    String imgUrl;
    BlogStatus status;
    public void validate() {
        if (title == null || title.trim().length() < 10) {
            throw new IllegalArgumentException("Tiêu đề phải có ít nhất 10 ký tự.");
        }

        if (content == null || content.trim().length() < 50) {
            throw new IllegalArgumentException("Nội dung bài viết phải có ít nhất 50 ký tự.");
        }

        if (status == null) {
            status = BlogStatus.DRAFT;
        }
    }
}
