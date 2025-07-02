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
@Schema(description = "Kết quả xét nghiệm gần đây")
public class RecentTestResultDTO {
    @Schema(description = "Tên xét nghiệm")
    String testName;
    
    @Schema(description = "Kết quả")
    String result;
    
    @Schema(description = "Ngày xét nghiệm")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate testDate;
    
    @Schema(description = "Có bất thường không")
    Boolean isAbnormal;
}
