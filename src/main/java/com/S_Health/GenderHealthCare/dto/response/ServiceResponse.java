package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.enums.ServiceType;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ServiceResponse {
    long id;
    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    Integer duration;
    ServiceType type;
    Double price;
    Boolean isActive;
    LocalDateTime createdAt;
}
