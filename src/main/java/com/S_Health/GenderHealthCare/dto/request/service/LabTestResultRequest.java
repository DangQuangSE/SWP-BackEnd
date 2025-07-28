package com.S_Health.GenderHealthCare.dto.request.service;

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
@Schema(description = "Request để nhập kết quả xét nghiệm")
public class LabTestResultRequest {
    @NotNull(message = "ID chi tiết cuộc hẹn không được để trống")
    @Positive(message = "ID chi tiết cuộc hẹn phải là số dương")
    @Schema(description = "ID của appointment detail liên quan đến xét nghiệm", example = "123")
    private Long appointmentDetailId;

    @NotBlank(message = "Mô tả kết quả không được để trống")
    @Size(min = 10, message = "Mô tả kết quả phải có ít nhất 10 ký tự")
    @Schema(description = "Mô tả về mục đích hoặc tình trạng xét nghiệm", 
            example = "Kiểm tra định kỳ HIV theo yêu cầu của bệnh nhân")
    private String description;

    @NotBlank(message = "Chẩn đoán không được để trống")
    @Size(min = 10, message = "Chẩn đoán phải có ít nhất 10 ký tự")
    @Schema(description = "Kết luận từ kết quả xét nghiệm", 
            example = "Âm tính với HIV, không phát hiện kháng thể")
    private String diagnosis;

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    @Size(min = 10, message = "Kế hoạch điều trị phải có ít nhất 10 ký tự")
    @Schema(description = "Hướng dẫn điều trị / theo dõi", 
            example = "Không cần điều trị, kiểm tra lại sau 6 tháng")
    private String treatmentPlan;

    // === Các trường riêng cho LAB TEST ===
    @NotBlank(message = "Tên xét nghiệm không được để trống")
    @Schema(description = "Tên xét nghiệm", example = "HIV Ag/Ab Combo Test")
    private String testName;

    @NotBlank(message = "Kết quả xét nghiệm không được để trống")
    @Schema(description = "Kết quả xét nghiệm", example = "Non-reactive")
    private String testResult;

    @Schema(description = "Giá trị bình thường", example = "Non-reactive")
    private String normalRange;

    @Schema(description = "Phương pháp xét nghiệm", example = "ELISA")
    private String testMethod;

    @Schema(description = "Loại mẫu xét nghiệm", example = "Blood")
    private String specimenType;

    @NotNull(message = "Trạng thái xét nghiệm không được để trống")
    @Schema(description = "Trạng thái kết quả xét nghiệm", example = "NORMAL")
    private TestStatus testStatus;

    @Schema(description = "Thời gian lấy mẫu", example = "2025-01-15T10:30:00")
    private LocalDateTime sampleCollectedAt;

    @Schema(description = "Ghi chú từ phòng lab", example = "Mẫu đạt chất lượng, kết quả tin cậy")
    private String labNotes;

    Long treatmentProtocolId;
}
