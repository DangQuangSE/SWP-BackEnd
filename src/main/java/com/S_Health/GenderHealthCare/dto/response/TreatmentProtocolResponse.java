package com.S_Health.GenderHealthCare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentProtocolResponse {

    Long id;
    String diseaseName;
    String diagnosis;
    String treatment;
    String followUp;
    String notes;
    boolean active;
}
