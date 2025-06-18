package com.S_Health.GenderHealthCare.dto.request.service;

import com.S_Health.GenderHealthCare.enums.Symptoms;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CycleTrackingRequest {
    Long userId;
    LocalDate startDate;
    Boolean isPeriodStart;
    List<Symptoms> symptoms;
    String node;
}
