package com.S_Health.GenderHealthCare.dto.request.blog;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
    Long bogId;
    String description;
}
