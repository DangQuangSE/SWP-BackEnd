package com.S_Health.GenderHealthCare.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentProtocolRequest {
    String diseaseName;
    String diagnosis;
    String treatment;
    String followUp;
    String notes;
}
