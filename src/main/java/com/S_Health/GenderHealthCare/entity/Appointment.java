package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;

    @ManyToOne
    @JoinColumn(name = "medicalProfile_id")
    MedicalProfile medicalProfile;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    ServiceSlotPool serviceSlotPool;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    User customer;
//    @ManyToOne
//    @JoinColumn(name = "consultant_id")
//    User consultant;

    String note;
    @Enumerated(EnumType.STRING)
    AppointmentStatus status;
    @CreationTimestamp
    LocalDateTime created_at;
    LocalDate preferredDate;
}
