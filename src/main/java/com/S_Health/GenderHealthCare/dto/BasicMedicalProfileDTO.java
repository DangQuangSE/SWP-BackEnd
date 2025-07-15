package com.S_Health.GenderHealthCare.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicMedicalProfileDTO {
    String allergies;                    // Dị ứng thuốc/thực phẩm
    String familyHistory;                // Tiền sử gia đình
    String chronicConditions;            // Bệnh mãn tính
    String specialNotes;                 // Ghi chú đặc biệt từ staff
}
