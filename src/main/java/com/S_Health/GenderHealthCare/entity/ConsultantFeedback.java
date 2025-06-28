package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultantFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long consultantId;

    @Min(1)
    @Max(5)
    int rating;

    LocalDateTime createAt;

    LocalDateTime updateAt;

    @Column(columnDefinition = "TEXT")
    String comment;

    @ManyToOne
    @JoinColumn(name = "service_feedback_id", nullable = false)
    ServiceFeedback serviceFeedback;


}
