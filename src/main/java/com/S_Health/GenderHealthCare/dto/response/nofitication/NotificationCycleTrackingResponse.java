package com.S_Health.GenderHealthCare.dto.response.nofitication;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCycleTrackingResponse {
    Long id;
    LocalDate cycleStartDate;
    Integer duration;
}
