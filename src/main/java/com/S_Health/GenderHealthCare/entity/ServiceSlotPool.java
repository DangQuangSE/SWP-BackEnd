package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceSlotPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "service_id")
    MedicalService service;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    int maxBooking;
    int currentBooking;
    int availableBooking;
    Boolean isActive;

    @OneToMany(mappedBy = "serviceSlotPool")
    List<Appointment> appointments;

}
