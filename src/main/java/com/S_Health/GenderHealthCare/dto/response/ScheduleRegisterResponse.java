package com.S_Health.GenderHealthCare.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleRegisterResponse {
    long consultant_id;
    List<WorkDateSlotResponse> schedules;
}
