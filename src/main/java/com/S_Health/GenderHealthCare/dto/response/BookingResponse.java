package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    long appointmentId;
    String customerName;
    LocalDate date;
    LocalTime time;
    String note;
    AppointmentStatus status;
    List<AppointmentDetailDTO> details;
}
