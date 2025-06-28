package com.S_Health.GenderHealthCare.dto.request.appointment;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAppointmentRequest {
     LocalDate preferredDate;
     Long slotId;
     String note;
    // Các trường chỉ cho phép staff/doctor/admin chỉnh
     AppointmentStatus status;
     Long consultantId;
     Double price;
}
