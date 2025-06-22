package com.S_Health.GenderHealthCare.dto.request.schedule;

import com.S_Health.GenderHealthCare.dto.TimeSlotDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleRegisterRequest {
   List<ScheduleItem> scheduleItems;
   @Data
   public static class ScheduleItem {
      private LocalDate workDate;
      TimeSlotDTO timeSlotDTO;
   }
}
