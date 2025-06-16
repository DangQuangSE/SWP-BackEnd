package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleConsultantResponse {
     LocalDate workDate;
     List<TimeSlotDTO> timeSlotDTOs;
}
