package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceSlotPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    int maxBooking;
    int currentBooking;
    int availableBooking;
    Boolean isActive;
    @Enumerated(EnumType.STRING)
    SlotStatus slotStatus;
    @OneToMany(mappedBy = "serviceSlotPool")
    List<Appointment> appointments;

}
