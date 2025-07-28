package com.S_Health.GenderHealthCare.dto.request.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request để nhập kết quả tư vấn khám bệnh")
public class ConsultationResultRequest {
    @NotNull(message = "ID chi tiết cuộc hẹn không được để trống")
    @Positive(message = "ID chi tiết cuộc hẹn phải là số dương")
    @Schema(description = "ID của appointment detail mà bác sĩ đang nhập kết quả", example = "123")
    private Long appointmentDetailId;

    @NotBlank(message = "Mô tả kết quả không được để trống")
    @Size(min = 10, message = "Mô tả kết quả phải có ít nhất 10 ký tự")
    @Schema(description = "Mô tả chi tiết về triệu chứng, vấn đề của bệnh nhân", 
            example = "Bệnh nhân có triệu chứng ngứa, đau rát vùng kín, có dịch tiết bất thường")
    private String description;

    @NotBlank(message = "Chẩn đoán không được để trống")
    @Size(min = 10, message = "Chẩn đoán phải có ít nhất 10 ký tự")
    @Schema(description = "Chẩn đoán của bác sĩ dựa trên kết quả khám", 
            example = "Viêm âm đạo do nấm Candida")
    private String diagnosis;

    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    @Size(min = 10, message = "Kế hoạch điều trị phải có ít nhất 10 ký tự")
    @Schema(description = "Kế hoạch điều trị, tư vấn hoặc theo dõi tiếp theo", 
            example = "Sử dụng thuốc kháng nấm, tái khám sau 1 tuần")
    private String treatmentPlan;

    Long treatmentProtocolId;
}
