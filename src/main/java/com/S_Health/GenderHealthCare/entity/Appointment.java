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
import java.util.List;

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

    @OneToMany(mappedBy = "appointment")
    List<AppointmentDetail> appointmentDetails;
    @ManyToOne
    @JoinColumn(name = "medicalProfile_id")
    MedicalProfile medicalProfile;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    ServiceSlotPool serviceSlotPool;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    User customer;
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    User consultant;
    //User updatedBy;
    @OneToMany(mappedBy = "appointment")
    List<Payment> payments;
    Double price;
    String note;
    @Enumerated(EnumType.STRING)
    AppointmentStatus status;
    @CreationTimestamp
    LocalDateTime created_at;
    LocalDateTime update_at;
    LocalDate preferredDate;
    Boolean isActive = true;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceFeedback> serviceFeedbacks;
}
