package com.S_Health.GenderHealthCare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    Long id;
    String commenterName;
    long commenterId;
    String commenterImageUrl;
    String description;
    LocalDateTime createAt;
}
