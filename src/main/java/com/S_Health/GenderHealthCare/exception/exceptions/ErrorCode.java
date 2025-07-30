package com.S_Health.GenderHealthCare.exception.exceptions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400, "Yêu cầu không hợp lệ"),
    INTERNAL_SERVER_ERROR(500, "Lỗi phía ở server"),
    UNKNOWN_ERROR(999, "Lỗi không xác định"),

    // Các lỗi của hệ thống

    INVALID_CREDENTIALS(100, "Email hoặc mật khẩu không chính xác"),
    PASSWORD_NOT_MATCH(101, "Mật khẩu không khớp"),
    USER_NOT_FOUND_BY_EMAIL(102, "Không tìm thấy người dùng với email"),
    ACCOUNT_DISABLED(401, "Tài khoản đã bị vô hiệu hóa"),
    INVALID_VERIFICATION_CODE(401, "Mã xác minh không chính xác"),

    APPOINTMENT_NOT_FOUND(400, "không tìm thấy lịch hẹn này"),






            ;
    int code;
    String message;
}
