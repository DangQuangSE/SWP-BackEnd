package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    long id;
    String name;
    String description;
    Integer duration;
    ServiceType type;
    Double price;
    Double discountPercent;
    Boolean isActive;
    Boolean isCombo;
    LocalDateTime createdAt;
    List<Long> subServiceIds;
    public ServiceDTO(long Id, String name, String description, Integer duration,
                          ServiceType type, Double price, Boolean isActive) {
        this.id = Id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.type = type;
        this.price = price;
        this.isActive = isActive;
        this.isCombo = false;
        this.discountPercent = null;
        this.subServiceIds = null;
    }
    public ServiceDTO(long Id, String name, String description, ServiceType type,
                          Double price, Boolean isActive, Double discountPercent, List<Long> subServiceIds) {
        this.id = Id;
        this.name = name;
        this.description = description;
        this.duration = null; // combo không có thời lượng cụ thể
        this.type = type;
        this.price = price;
        this.isActive = isActive;
        this.isCombo = true;
        this.discountPercent = discountPercent;
        this.subServiceIds = subServiceIds;
    }
}
