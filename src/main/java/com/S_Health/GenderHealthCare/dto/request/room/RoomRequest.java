package com.S_Health.GenderHealthCare.dto.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    @NotBlank(message = "Room name is required")
    String name;
    String description;
    @NotNull(message = "ID chuyên môn không được để trống")
    Long specializationId;
}
