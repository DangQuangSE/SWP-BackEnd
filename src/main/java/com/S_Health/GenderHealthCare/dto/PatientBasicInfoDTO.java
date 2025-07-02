package com.S_Health.GenderHealthCare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Thông tin cơ bản bệnh nhân")
public class PatientBasicInfoDTO {
    @Schema(description = "Họ và tên")
    String fullname;

    @Schema(description = "Tuổi")
    Integer age;

    @Schema(description = "Giới tính")
    String gender;

    @Schema(description = "Email")
    String email;

    @Schema(description = "Số điện thoại")
    String phone;

    // === THÔNG TIN Y TẾ QUAN TRỌNG ===
    @Schema(description = "Dị ứng thuốc/thực phẩm", nullable = true)
    String allergies;

    @Schema(description = "Tiền sử gia đình", nullable = true)
    String familyHistory;

    @Schema(description = "Thói quen sinh hoạt", nullable = true)
    String lifestyleNotes;

    @Schema(description = "Ghi chú đặc biệt", nullable = true)
    String specialNotes;
}
