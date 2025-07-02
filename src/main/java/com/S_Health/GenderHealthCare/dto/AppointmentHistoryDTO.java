package com.S_Health.GenderHealthCare.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Lịch sử khám bệnh")
public class AppointmentHistoryDTO {
    @Schema(description = "Ngày khám")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date;
    
    @Schema(description = "Dịch vụ")
    String service;
    
    @Schema(description = "Bác sĩ")
    String doctor;

    @Schema(description = "Phòng khám")
    String room;

    @Schema(description = "Trạng thái")
    String status;

    @Schema(description = "Chẩn đoán")
    String diagnosis;
}
