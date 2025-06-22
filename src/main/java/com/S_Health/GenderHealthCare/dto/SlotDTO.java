package com.S_Health.GenderHealthCare.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlotDTO {
    Long slotId;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    int maxBooking;
    int currentBooking;
    int availableBooking;
}