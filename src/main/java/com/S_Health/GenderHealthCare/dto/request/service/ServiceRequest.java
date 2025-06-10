package com.S_Health.GenderHealthCare.dto.request.service;

import com.S_Health.GenderHealthCare.enums.ServiceType;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRequest {

    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    Integer duration;
    ServiceType type;
    Double price;
    Boolean isActive;
}
