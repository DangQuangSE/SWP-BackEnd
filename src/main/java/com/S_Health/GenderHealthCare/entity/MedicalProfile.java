package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
   // String note;
    Boolean isActive = true;
}
