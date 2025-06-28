package com.S_Health.GenderHealthCare.dto;

import com.S_Health.GenderHealthCare.dto.response.MedicalProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryDTO {
    private MedicalProfileDTO medicalProfile;
    private List<AppointmentDTO> pastAppointments;
}
