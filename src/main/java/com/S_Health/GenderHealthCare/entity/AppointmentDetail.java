package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;

    @ManyToOne
    @JoinColumn(name = "consultant_id")
    User consultant;

    LocalTime slotTime;
    @Enumerated(EnumType.STRING)
    AppointmentStatus status = AppointmentStatus.PENDING;
}
