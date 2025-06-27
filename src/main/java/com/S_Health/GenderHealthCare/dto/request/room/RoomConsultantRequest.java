package com.S_Health.GenderHealthCare.dto.request.room;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class RoomConsultantRequest {
    @NotNull(message = "Consultant ID is required")
    private Long consultantId;

    @NotNull(message = "Working day is required")
    private DayOfWeek workingDay;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
