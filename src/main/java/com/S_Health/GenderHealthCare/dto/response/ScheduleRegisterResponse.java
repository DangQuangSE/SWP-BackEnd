package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.entity.Schedule;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleRegisterResponse {
    long consultant_id;
    List<ScheduleConsultantResponse> schedules;
}
