package com.S_Health.GenderHealthCare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TreatmentProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diseaseName;

    @Lob
    private String diagnosis;      // Triệu chứng, chẩn đoán

    @Lob
    private String treatment;      // Phác đồ điều trị

    @Lob
    private String followUp;       // Kế hoạch theo dõi

    @Lob
    private String notes;          // Lưu ý đặc biệt

    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
