package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.TagDTO;
import com.S_Health.GenderHealthCare.dto.UserDTO;
import com.S_Health.GenderHealthCare.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;
    private int viewCount;
    private int likeCount;
    private BlogStatus status;
    private UserDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagDTO> tags;
}
