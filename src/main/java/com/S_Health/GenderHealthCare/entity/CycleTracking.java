package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.Symptoms;
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
    Boolean isPeriodStart;

    @Enumerated(EnumType.STRING)
    Symptoms symptoms;

    @Column(columnDefinition = "TEXT")
    String note;

    LocalDateTime createdAt;
}
