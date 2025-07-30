package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.SlotDTO;
import com.S_Health.GenderHealthCare.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWorkingScheduleDTO {
    private UserDTO doctor;
    private LocalDate workDate;
    private List<SlotDTO> slots;
}
