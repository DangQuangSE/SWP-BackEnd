package com.S_Health.GenderHealthCare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CycleTrackingResponse {
    long id;
    LocalDate startDate;
    String note;
    Boolean isPeriodStart;
}
