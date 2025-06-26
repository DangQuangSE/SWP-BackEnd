package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class AppointmentStatsDTO {
    int totalToday;
    int pending;
    int confirmed;
    int completed;
    int canceled;
    int absent;
}
