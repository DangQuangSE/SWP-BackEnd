package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentAuditLogDTO {
    private Long id;
    private Long appointmentId;
    private AppointmentStatus oldStatus;
    private AppointmentStatus newStatus;
    private String updatedByUsername;
    private String userRole;
    private String note;
    private LocalDateTime createdAt;
}
