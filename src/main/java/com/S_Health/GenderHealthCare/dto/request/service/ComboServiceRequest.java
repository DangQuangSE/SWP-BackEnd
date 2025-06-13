package com.S_Health.GenderHealthCare.dto.request.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboServiceRequest {
    String name;
    String description;
    Double discountPercent;
    List<Long> subServiceIds;
}
