package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultantSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    // Bác sĩ phụ trách slot này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id")
     User consultant;
     LocalDate date;
     LocalTime startTime;
     LocalTime endTime;
     int maxBooking;
     int currentBooking;
     int availableBooking;
    @Enumerated(EnumType.STRING)
     SlotStatus status;

    @CreationTimestamp
     LocalDateTime createdAt;

    @UpdateTimestamp
     LocalDateTime updatedAt;
}
