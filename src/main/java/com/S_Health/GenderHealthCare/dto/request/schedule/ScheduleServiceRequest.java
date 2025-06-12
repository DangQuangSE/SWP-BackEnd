package com.S_Health.GenderHealthCare.dto.request.schedule;

import com.S_Health.GenderHealthCare.dto.RangeDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;


@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleServiceRequest {
    long service_id;
    RangeDate rangeDate;
}
