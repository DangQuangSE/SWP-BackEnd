package com.S_Health.GenderHealthCare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecializationRequest {
    @NotBlank(message = "Tên chuyên môn không được để trống")
    String name;

    @NotNull(message = "ID dịch vụ không được để trống")
    Long serviceId;
}
