package com.S_Health.GenderHealthCare.enums;

public enum AppointmentStatus {
    PENDING,        // Mới tạo, chờ confirm
    CONFIRMED,      // Đã xác nhận
    CHECKED,     // Bệnh nhân đã đến, đang chờ khám
    IN_PROGRESS,    // Đang được khám
    WAITING_RESULT, // Đang chờ kết quả cận lâm sàng
    COMPLETED,      // Khám xong, có kết quả
    CANCELED,       // Đã huỷ
    ABSENT,         // Bệnh nhân báo trước không đế
    DELETED
}
