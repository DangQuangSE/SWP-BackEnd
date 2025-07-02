package com.S_Health.GenderHealthCare.enums;

/**
 * Enum định nghĩa trạng thái kết quả xét nghiệm
 */
public enum TestStatus {
    NORMAL,         // Bình thường
    ABNORMAL,       // Bất thường  
    CRITICAL,       // Nguy hiểm, cần can thiệp ngay
    PENDING,        // Chờ kết quả
    INVALID         // Mẫu không hợp lệ, cần lấy lại
}
