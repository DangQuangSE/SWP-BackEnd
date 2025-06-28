package com.S_Health.GenderHealthCare.dto.request.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultRequest {
    long detail_id;
    @NotBlank(message = "Mô tả kết quả không được để trống")
    @Size(min = 20, message = "Mô tả kết quả phải có ít nhất 20 ký tự")
    String description;
    @NotBlank(message = "Chẩn đoán kết quả không được để trống")
    @Size(min = 20, message = "Chẩn đoán kết quả phải có ít nhất 20 ký tự")
    String diagnosis;
    @NotBlank(message = "Kế hoạch điều trị không được để trống")
    @Size(min = 20, message = "Kế hoạch điều trị phải có ít nhất 20 ký tự")
    String treatmentPlan;
}
