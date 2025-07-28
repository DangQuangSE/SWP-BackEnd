package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.enums.TestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultDTO {
    // === METADATA ===
    Long id;
    ResultType resultType;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Boolean isActive;

    // === THÔNG TIN CHUNG ===
    String description;          // Mô tả triệu chứng/vấn đề
    String diagnosis;            // Chẩn đoán
    String treatmentPlan;        // Kế hoạch điều trị/tư vấn

    // === THÔNG TIN XÉT NGHIỆM (chỉ có khi resultType = LAB_TEST) ===
    String testName;             // Tên xét nghiệm
    String testResult;           // Kết quả xét nghiệm
    String normalRange;          // Giá trị bình thường
    String testMethod;           // Phương pháp xét nghiệm
    String specimenType;         // Loại mẫu
    TestStatus testStatus;       // Trạng thái kết quả
    LocalDateTime sampleCollectedAt; // Thời gian lấy mẫu
    String labNotes;             // Ghi chú từ phòng lab

    // === THÔNG TIN LIÊN QUAN ===
    Long appointmentDetailId;    // ID của appointment detail
    String consultantName;       // Tên bác sĩ nhập kết quả
    String serviceName;          // Tên dịch vụ
    String patientName;          // Tên bệnh nhân (để dễ hiển thị)

    Long treatmentProtocolId;
}
