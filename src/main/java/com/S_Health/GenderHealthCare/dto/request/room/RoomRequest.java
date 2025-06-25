package com.S_Health.GenderHealthCare.dto.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RoomRequest {
    @NotBlank(message = "Room name is required")
    private String name;

    private String description;

    @NotBlank(message = "Room location is required")
    private String location;

    private int capacity;

    private String facilities;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    @NotNull(message = "Specialization ID is required")
    private Long specializationId;
}
