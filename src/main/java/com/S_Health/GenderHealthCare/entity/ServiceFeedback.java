package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Min(1)
    @Max(5)
    double rating;

    @Column(columnDefinition = "TEXT")
    String comment;

    LocalDateTime createAt;

    LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    Appointment appointment;

    @OneToMany(mappedBy = "serviceFeedback", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ConsultantFeedback> consultantFeedbacks;

}
