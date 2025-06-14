package com.S_Health.GenderHealthCare.dto.request.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.modelmapper.internal.bytebuddy.asm.Advice;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    long service_id;
    LocalDate preferredDate;
    LocalTime slot;
    String note;
}
