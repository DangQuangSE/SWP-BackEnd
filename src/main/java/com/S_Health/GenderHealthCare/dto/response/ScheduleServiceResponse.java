package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.dto.ServiceDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleServiceResponse {
    ServiceDTO serviceDTO;
    List<ScheduleConsultantResponse> scheduleResponses;
}
