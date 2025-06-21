package com.S_Health.GenderHealthCare.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ScheduleRegisterResponse {
    long consultant_id;
    List<WorkDate> schedules;
    @Data
    public static class WorkDate{
        LocalDate date;
        LocalTime start;
        LocalTime end;
    }
}
