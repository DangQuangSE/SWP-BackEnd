package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long appointmentId;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    AppointmentStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    AppointmentStatus newStatus;
    Long updatedByUserId;
    String updatedByUsername;
    String userRole;
    String note;
    @CreationTimestamp
    LocalDateTime createdAt;
}
