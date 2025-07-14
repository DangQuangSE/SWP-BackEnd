package com.S_Health.GenderHealthCare.dto.request.service;

import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.enums.TestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request để nhập kết quả khám bệnh/xét nghiệm")
public class ResultRequest {
    @NotNull(message = "ID chi tiết cuộc hẹn không được để trống")
    @Positive(message = "ID chi tiết cuộc hẹn phải là số dương")
    @Schema(description = "ID của appointment detail mà bác sĩ đang nhập kết quả", example = "123")
    Long appointmentDetailId;

    @NotNull(message = "Loại kết quả không được để trống")
    @Schema(description = "Loại kết quả: CONSULTATION (tư vấn) hoặc LAB_TEST (xét nghiệm)", example = "LAB_TEST")
    ResultType resultType;

    // === THÔNG TIN CHUNG ===
    @NotBlank(message = "Mô tả kết quả không được để trống")
    @Size(min = 10, message = "Mô tả kết quả phải có ít nhất 10 ký tự")
    @Schema(description = "Mô tả chi tiết về triệu chứng, vấn đề hoặc quá trình xét nghiệm",
            example = "Bệnh nhân có triệu chứng ngứa, đau rát vùng kín")
    String description;

    @NotBlank(message = "Chẩn đoán kết quả không được để trống")
    @Size(min = 10, message = "Chẩn đoán kết quả phải có ít nhất 10 ký tự")
    @Schema(description = "Chẩn đoán của bác sĩ dựa trên kết quả khám/xét nghiệm",
            example = "Không phát hiện HIV")
    String diagnosis;

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    @Size(min = 10, message = "Kế hoạch điều trị phải có ít nhất 10 ký tự")
    @Schema(description = "Kế hoạch điều trị, tư vấn hoặc theo dõi tiếp theo",
            example = "Tiếp tục theo dõi, xét nghiệm định kỳ 6 tháng/lần")
    String treatmentPlan;

    // === THÔNG TIN XÉT NGHIỆM (optional, chỉ dành cho LAB_TEST) ===
    @Schema(description = "Tên xét nghiệm (chỉ dành cho LAB_TEST)", example = "HIV Ag/Ab Combo Test")
    String testName;

    @Schema(description = "Kết quả xét nghiệm (chỉ dành cho LAB_TEST)", example = "Non-reactive")
    String testResult;

    @Schema(description = "Giá trị bình thường (chỉ dành cho LAB_TEST)", example = "Non-reactive")
    String normalRange;

    @Schema(description = "Phương pháp xét nghiệm (chỉ dành cho LAB_TEST)", example = "ELISA")
    String testMethod;

    @Schema(description = "Loại mẫu xét nghiệm (chỉ dành cho LAB_TEST)", example = "Blood")
    String specimenType;

    @Schema(description = "Trạng thái kết quả xét nghiệm (chỉ dành cho LAB_TEST)", example = "NORMAL")
    TestStatus testStatus;

    @Schema(description = "Thời gian lấy mẫu (chỉ dành cho LAB_TEST)", example = "2025-01-15T10:30:00")
    LocalDateTime sampleCollectedAt;

    @Schema(description = "Ghi chú từ phòng lab (chỉ dành cho LAB_TEST)", example = "Mẫu đạt chất lượng, kết quả tin cậy")
    String labNotes;
}
