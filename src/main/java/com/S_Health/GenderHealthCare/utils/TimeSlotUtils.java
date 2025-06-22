package com.S_Health.GenderHealthCare.utils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
@Component
public class TimeSlotUtils {
    public static List<LocalTime> generateSlots(LocalTime start, LocalTime end, Duration slotLength) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = start;

        while (!current.plus(slotLength).isAfter(end)) {
            slots.add(current);
            current = current.plus(slotLength);
        }
        return slots;
    }
}
