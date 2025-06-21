package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User customer;
    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;
    @OneToMany(mappedBy = "medicalProfile", cascade = CascadeType.ALL)
    List<Appointment> appointments;
    String result;
    String note;
    @CreationTimestamp
    LocalDateTime create_at;
}
