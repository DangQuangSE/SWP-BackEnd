package com.S_Health.GenderHealthCare.exception.exceptions;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppsException extends RuntimeException {
    ErrorCode errorCode;
}
