package com.S_Health.GenderHealthCare.dto.response.consultant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantCertification {
    String name;
    String imageUrl;
}
