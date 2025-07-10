package com.S_Health.GenderHealthCare.dto.response.report;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceBookingReportDTO {
    Long serviceId;
    String serviceName;
    Long totalBookings;
    Long totalCancellations;
}
