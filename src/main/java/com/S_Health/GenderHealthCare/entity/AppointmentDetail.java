package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "room_id")
    Room room;

    LocalDateTime slotTime;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    AppointmentStatus status = AppointmentStatus.PENDING;
    @Column(columnDefinition = "TEXT")
    String joinUrl;
    @Column(length = 1000)
    String startUrl;

    @OneToOne(mappedBy = "appointmentDetail")
    MedicalResult medicalResult;
    Boolean isActive = true;
    LocalDateTime update_at;
    LocalDateTime create_at;
}
