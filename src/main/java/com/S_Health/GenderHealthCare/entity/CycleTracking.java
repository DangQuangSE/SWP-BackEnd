package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CycleTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany(mappedBy = "cycleTracking")
    List<Notification> notifications;

    LocalDate startDate;
    LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    String note;

    LocalDateTime createdAt;
}
