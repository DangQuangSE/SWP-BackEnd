package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HospitalSlotFree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    LocalDate date;
    LocalTime start;
    LocalTime end;
    @ManyToOne
    @JoinColumn(name = "specialization_id")
    Specialization specialization;
    int maxBooking;
    int currentBooking;
}
