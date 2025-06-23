package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class AppointmentDetailDTO {
    long id;
    long serviceId;
    String serviceName;
    long consultantId;
    String consultantName;
    LocalDateTime slotTime;
    AppointmentStatus status;
    ResultDTO medicalResult;
}
