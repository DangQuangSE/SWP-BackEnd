package com.S_Health.GenderHealthCare.service.appointment;

import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service chuyên dụng để tính toán trạng thái Appointment dựa trên các AppointmentDetail
 */
@Component
public class AppointmentStatusCalculator {

    /**
     * Tính toán trạng thái Appointment dựa trên các AppointmentDetail
     * Quy tắc:
     * - Có 1 detail IN_PROGRESS → Appointment IN_PROGRESS
     * - Tất cả detail WAITING_RESULT → Appointment WAITING_RESULT
     * - Tất cả detail COMPLETED → Appointment COMPLETED
     */
    public AppointmentStatus calculateStatus(List<AppointmentDetail> details) {
        if (details.isEmpty()) return AppointmentStatus.PENDING;
        
        // Lọc ra các detail đang active
        List<AppointmentDetail> activeDetails = details.stream()
                .filter(d -> d.getIsActive())
                .collect(Collectors.toList());
        
        if (activeDetails.isEmpty()) return AppointmentStatus.PENDING;
        
        // Quy tắc 1: Có 1 detail IN_PROGRESS → Appointment IN_PROGRESS
        boolean hasInProgress = activeDetails.stream()
                .anyMatch(d -> d.getStatus() == AppointmentStatus.IN_PROGRESS);
        if (hasInProgress) {
            return AppointmentStatus.IN_PROGRESS;
        }
        
        // Quy tắc 3: Tất cả detail COMPLETED → Appointment COMPLETED
        boolean allCompleted = activeDetails.stream()
                .allMatch(d -> d.getStatus() == AppointmentStatus.COMPLETED);
        if (allCompleted) {
            return AppointmentStatus.COMPLETED;
        }
        
        // Quy tắc 2: Tất cả detail WAITING_RESULT → Appointment WAITING_RESULT
        boolean allWaitingResult = activeDetails.stream()
                .allMatch(d -> d.getStatus() == AppointmentStatus.WAITING_RESULT);
        if (allWaitingResult) {
            return AppointmentStatus.WAITING_RESULT;
        }
        
        // Nếu tất cả CHECKED → Appointment CHECKED
        boolean allChecked = activeDetails.stream()
                .allMatch(d -> d.getStatus() == AppointmentStatus.CHECKED);
        if (allChecked) {
            return AppointmentStatus.CHECKED;
        }
        
        // Trường hợp khác → giữ nguyên status hiện tại
        return AppointmentStatus.PENDING;
    }
}
