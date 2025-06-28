package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentDTO {
    long id;
    Double price;
    String note;
    LocalDate preferredDate;
    LocalDateTime created_at;
    AppointmentStatus status;
    String customerName;
    String serviceName;
    Boolean isPaid;
    PaymentStatus paymentStatus;
    List<AppointmentDetailDTO> appointmentDetails;
}
