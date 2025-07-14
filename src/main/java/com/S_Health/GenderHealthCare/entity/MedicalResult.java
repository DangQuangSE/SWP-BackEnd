package com.S_Health.GenderHealthCare.entity;

import com.S_Health.GenderHealthCare.enums.ResultType;
import com.S_Health.GenderHealthCare.enums.TestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Loại kết quả: TƯ VẤN hoặc XÉT NGHIỆM
    @Enumerated(EnumType.STRING)
    @Column(name = "result_type")
    ResultType resultType;

    // === THÔNG TIN CHUNG ===
    @Column(columnDefinition = "TEXT")
    String description;          // Mô tả triệu chứng/vấn đề

    @Column(columnDefinition = "TEXT")
    String diagnosis;            // Chẩn đoán

    @Column(columnDefinition = "TEXT")
    String treatmentPlan;        // Kế hoạch điều trị/tư vấn

    // === THÔNG TIN XÉT NGHIỆM (chỉ dành cho LAB_TEST) ===
    @Column(name = "test_name")
    String testName;             // Tên xét nghiệm: "HIV Ag/Ab Combo", "Chlamydia PCR"

    @Column(name = "test_result")
    String testResult;           // Kết quả: "Negative", "Positive", "1.2 Index"

    @Column(name = "normal_range")
    String normalRange;          // Giá trị bình thường: "< 1.0", "Non-reactive"

    @Column(name = "test_method")
    String testMethod;           // Phương pháp: "ELISA", "PCR", "Rapid Test"

    @Column(name = "specimen_type")
    String specimenType;         // Loại mẫu: "Blood", "Urine", "Swab"

    @Enumerated(EnumType.STRING)
    @Column(name = "test_status")
    TestStatus testStatus;       // NORMAL, ABNORMAL, CRITICAL, PENDING

    @Column(name = "sample_collected_at")
    LocalDateTime sampleCollectedAt;  // Thời gian lấy mẫu

    @Column(name = "lab_notes", columnDefinition = "TEXT")
    String labNotes;             // Ghi chú từ phòng lab

    // === QUAN HỆ VÀ METADATA ===
    @OneToOne
    @JoinColumn(name = "appointment_detail_id")
    @JsonIgnore
    AppointmentDetail appointmentDetail;

    @ManyToOne
    @JoinColumn(name = "entered_by")
    User consultant;             // Bác sĩ nhập kết quả

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Builder.Default
    Boolean isActive = true;
}
