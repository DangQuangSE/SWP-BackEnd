package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String name;
    @Column(columnDefinition = "TEXT")
    String description;
    Integer duration;
    @Enumerated(EnumType.STRING)
    ServiceType type;
    Double price;
    Boolean isActive;
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "service")
    List<Specialization> specializations;

    @OneToMany(mappedBy = "service")
    List<ServiceSlotPool> serviceSlotPools;

    @OneToMany(mappedBy = "service")
    List<MedicalProfile> medicalProfiles;

    @OneToMany(mappedBy = "service")
    List<Appointment> appointments;
}
