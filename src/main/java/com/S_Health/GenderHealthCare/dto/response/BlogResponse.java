package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BlogResponse {
    String title;
    String content;
    String imgUrl;
    User author;
    LocalDateTime createdAt;
}
