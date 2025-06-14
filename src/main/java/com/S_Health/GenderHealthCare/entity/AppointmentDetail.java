package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
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

    Double priceInCombo;

    @Enumerated(EnumType.STRING)
    AppointmentStatus status = AppointmentStatus.PENDING;
}
