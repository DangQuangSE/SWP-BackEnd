package com.S_Health.GenderHealthCare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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
    Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    User customer;

    @ManyToOne
    @JoinColumn(name = "service_id")
    Service service;

    // === THÔNG TIN Y TẾ CƠ BẢN ===
    @Column(name = "blood_type", length = 10)
    String bloodType;                    // Nhóm máu: A, B, AB, O

    @Column(name = "allergies", columnDefinition = "TEXT")
    String allergies;                    // Dị ứng thuốc/thực phẩm

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    String chronicConditions;            // Bệnh mãn tính (bác sĩ có thể cập nhật)

    @Column(name = "emergency_contact", length = 500)
    String emergencyContact;             // Liên hệ khẩn cấp

    @Column(name = "family_history", columnDefinition = "TEXT")
    String familyHistory;                // Tiền sử gia đình (staff nhập)

    @Column(name = "lifestyle_notes", columnDefinition = "TEXT")
    String lifestyleNotes;               // Ghi chú lối sống (staff nhập)

    @Column(name = "special_notes", columnDefinition = "TEXT")
    String specialNotes;                 // Ghi chú đặc biệt từ staff

    // === METADATA ===
    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "last_updated_by")
    Long lastUpdatedBy;                  // ID của staff/doctor cập nhật cuối

    @Builder.Default
    @Column(name = "is_active")
    Boolean isActive = true;

    // === RELATIONSHIPS ===
    @OneToMany(mappedBy = "medicalProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    List<Appointment> appointments;
}
