package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
    String title;
    String content;
    String imgUrl;
    UserDTO author;
    LocalDateTime createdAt;
}
