package com.S_Health.GenderHealthCare.dto.request.schedule;

import com.S_Health.GenderHealthCare.dto.RangeDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleConsultantRequest {
    long consultant_id;
    RangeDate rangeDate;
}
