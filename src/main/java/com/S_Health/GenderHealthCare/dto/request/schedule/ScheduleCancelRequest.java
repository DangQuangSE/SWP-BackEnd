package com.S_Health.GenderHealthCare.dto.request.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCancelRequest {
    private LocalDate date;
    private LocalTime slot;
    private String reason;

    public boolean isCancelWholeDay() {
        return slot == null;
    }
}
