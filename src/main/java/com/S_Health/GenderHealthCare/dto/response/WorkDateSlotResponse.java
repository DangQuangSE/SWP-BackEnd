package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.SlotDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkDateSlotResponse {
     LocalDate workDate;
     List<SlotDTO> slots;
}
