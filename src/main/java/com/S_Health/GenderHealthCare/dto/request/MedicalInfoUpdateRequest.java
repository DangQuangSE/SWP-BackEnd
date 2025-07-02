package com.S_Health.GenderHealthCare.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request cập nhật thông tin cơ bản khi check-in (dành cho staff)")
public class MedicalInfoUpdateRequest {

    @NotNull(message = "ID bệnh nhân không được để trống")
    @Schema(description = "ID bệnh nhân", example = "123")
    Long customerId;

    @NotNull(message = "ID dịch vụ không được để trống")
    @Schema(description = "ID dịch vụ", example = "1")
    Long serviceId;

    // === THÔNG TIN TỰ KHAI CỦA BỆNH NHÂN (Staff thu thập khi check-in) ===

    @Schema(description = "Dị ứng thuốc/thực phẩm (bệnh nhân tự khai)",
            example = "Penicillin, Tôm cua",
            nullable = true)
    String allergies;

    @Schema(description = "Bệnh mãn tính (nếu bệnh nhân biết)",
            example = "Cao huyết áp, Tiểu đường",
            nullable = true)
    String chronicConditions;

    @Schema(description = "Tiền sử gia đình về bệnh tật (bệnh nhân tự khai)",
            example = "Cha mắc tim mạch, mẹ mắc tiểu đường",
            nullable = true)
    String familyHistory;

    @Schema(description = "Thói quen sinh hoạt (bệnh nhân tự khai)",
            example = "Hút thuốc 10 điếu/ngày, uống rượu cuối tuần, tập thể dục 3 lần/tuần",
            nullable = true)
    String lifestyleNotes;

    @Schema(description = "Ghi chú đặc biệt từ bệnh nhân",
            example = "Bệnh nhân lo lắng, sợ tiêm, cần giải thích kỹ",
            nullable = true)
    String specialNotes;

    @Schema(description = "Liên hệ khẩn cấp",
            example = "Nguyễn Thị B (vợ) - 0987654321",
            nullable = true)
    String emergencyContact;
}
