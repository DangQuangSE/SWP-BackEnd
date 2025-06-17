package com.S_Health.GenderHealthCare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlogSummaryDTO {
     long blog_id;
     String title;
     int viewCount;
     int likeCount;
     long commentCount;
}
