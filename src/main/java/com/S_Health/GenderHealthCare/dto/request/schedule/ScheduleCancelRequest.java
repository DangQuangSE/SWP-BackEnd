package com.S_Health.GenderHealthCare.dto.request.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCancelRequest {
    long consultant_id;
    LocalDateTime date;
    String reason;
}
