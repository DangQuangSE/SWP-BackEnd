package com.S_Health.GenderHealthCare.dto.request.schedule;

import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InputScheduleRequest {
   ScheduleConsultantRequest scheduleConsultantRequest;
   TimeSlotDTO timeSlotDTO;
}
