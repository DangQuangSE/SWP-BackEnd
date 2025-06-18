package com.S_Health.GenderHealthCare.dto.response;

import com.S_Health.GenderHealthCare.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCancelResponse {
    String message;
    List<AffectedAppointment> affectedAppointments;
    @Data
    @AllArgsConstructor
    public static class AffectedAppointment {
        User customer;
        LocalDate date;
        String status;
    }
}
