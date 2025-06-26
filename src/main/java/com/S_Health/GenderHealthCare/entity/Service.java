package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    Integer duration;

    @Enumerated(EnumType.STRING)
    ServiceType type;

    Double price;
    Double discountPercent = 0.0;

    Boolean isActive = true;
    Boolean isCombo = false;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // Quan hệ nhiều-nhiều với Specialization
    @ManyToMany
    @JoinTable(
        name = "service_specialization",
        joinColumns = @JoinColumn(name = "service_id"),
        inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    List<Specialization> specializations = new ArrayList<>();

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    List<ServiceSlotPool> serviceSlotPools = new ArrayList<>();

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    List<MedicalProfile> medicalProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    List<Appointment> appointments = new ArrayList<>();


    @OneToMany(mappedBy = "comboService")
    @JsonIgnore
    List<ComboItem> comboItems;

    @OneToMany(mappedBy = "subService")
    @JsonIgnore
    List<ComboItem> subServiceItems;

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    List<AppointmentDetail> appointmentDetails = new ArrayList<>();
}