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
}
