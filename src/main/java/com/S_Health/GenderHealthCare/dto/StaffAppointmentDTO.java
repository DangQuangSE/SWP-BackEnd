package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class StaffAppointmentDTO {
    Long id;
    String customerName;
    Long customerId;
    String doctorName;
    Long doctorId;
    LocalDateTime appointmentTime;
    String status;
    boolean checkedIn;
}
