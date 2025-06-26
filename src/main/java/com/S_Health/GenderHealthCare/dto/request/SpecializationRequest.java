package com.S_Health.GenderHealthCare.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecializationRequest {
    @NotBlank(message = "Tên chuyên môn không được để trống")
    private String name;

    private String description;
}