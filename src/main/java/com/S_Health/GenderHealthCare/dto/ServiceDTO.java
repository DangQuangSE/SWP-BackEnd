package com.S_Health.GenderHealthCare.dto;


import com.S_Health.GenderHealthCare.enums.ServiceType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Integer duration;
    private ServiceType type;
    private Double price;
    private Double discountPercent;
    private Boolean isActive;
    private Boolean isCombo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Danh sách ID các chuyên môn
    private List<Long> specializationIds;

    // Danh sách các chuyên môn đầy đủ
    private List<SpecializationDTO> specializations;

    // Dùng cho combo service
    private List<Long> subServiceIds;
}
