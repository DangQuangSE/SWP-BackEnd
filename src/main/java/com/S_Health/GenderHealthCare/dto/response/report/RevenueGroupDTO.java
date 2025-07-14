package com.S_Health.GenderHealthCare.dto.response.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RevenueGroupDTO {
    String serviceCategory;
    Integer year;
    Integer month;
    BigDecimal totalAmount;
}
