package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@Builder
public class AppointmentDetailDTO {
    long id;
    String serviceName;
    String consultantName;
    LocalTime startTime;
    AppointmentStatus status;
}
