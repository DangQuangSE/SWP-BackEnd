package com.S_Health.GenderHealthCare.service.audit;

import com.S_Health.GenderHealthCare.entity.AppointmentAuditLog;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.repository.AppointmentAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentAuditService {

    @Autowired
    private AppointmentAuditLogRepository auditLogRepository;

    public void logStatusChange(
            Long appointmentId,
            AppointmentStatus oldStatus,
            AppointmentStatus newStatus,
            User updatedBy,
            String note) {

        AppointmentAuditLog auditLog = new AppointmentAuditLog();
        auditLog.setAppointmentId(appointmentId);
        auditLog.setOldStatus(oldStatus);
        auditLog.setNewStatus(newStatus);
        auditLog.setUpdatedByUserId(updatedBy.getId());
        auditLog.setUpdatedByUsername(updatedBy.getUsername());
        auditLog.setUserRole(updatedBy.getRole().toString());
        auditLog.setNote(note);

        auditLogRepository.save(auditLog);
    }

    public List<AppointmentAuditLog> getAuditLogsForAppointment(Long appointmentId) {
        return auditLogRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
}
