package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String description;
    String diagnosis;
    String treatmentPlan;
    @OneToOne
    @JoinColumn(name = "appointment_detail_id")
    AppointmentDetail appointmentDetail;
    @CreationTimestamp
    LocalDateTime createAt;
    @ManyToOne
    @JoinColumn(name = "entered_by")
    User consultant;
    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    Boolean isActive;
}
