package com.S_Health.GenderHealthCare.api;
import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.request.appointment.UpdateAppointmentRequest;
import com.S_Health.GenderHealthCare.entity.AppointmentAuditLog;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import com.S_Health.GenderHealthCare.service.audit.AppointmentAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/appointment")
@SecurityRequirement(name = "api")
public class AppointmentAPI {
    @Autowired
    AppointmentService appointmentService;
    @Autowired
    AppointmentAuditService auditService;
    @GetMapping("{id}")
    public ResponseEntity getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }
    //hiển thị cho bác sĩ theo status của appointment detail
    @GetMapping("/my-schedule")
    @Operation(summary = "Lấy lịch làm việc của bác sĩ theo ngày và trạng thái detail",
               description = "Trả về danh sách appointment có chứa appointment detail của bác sĩ hiện tại với trạng thái được chỉ định")
    public ResponseEntity<List<AppointmentDTO>> getMySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @Parameter(description = "Trạng thái của appointment detail (không phải appointment)")
            AppointmentStatus status) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForConsultantOnDate(date, status);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/by-status")
    @Operation(summary = "Lấy danh sách lịch hẹn theo trạng thái", 
               description = "Trả về danh sách lịch hẹn theo trạng thái (PENDING, BOOKED, CHECKED, COMPLETED, CANCELED). Kết quả phụ thuộc vào vai trò người dùng: khách hàng chỉ xem được lịch hẹn của mình, bác sĩ xem được lịch hẹn của bệnh nhân của họ, admin/staff xem được tất cả.")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }
    @PostMapping("/{id}")
    public ResponseEntity updateAppointmentById(@PathVariable Long id, @RequestBody UpdateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteAppointmentById(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
    //cập nhật trạng thái checked cho appointment
    @PatchMapping("/{id}/checkin")
    public ResponseEntity checkInAppointment(@PathVariable Long id){
        appointmentService.checkInAppointment(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{appointmentId}/patient-history")
    public ResponseEntity getPatientHistory(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getPatientHistoryFromAppointment(appointmentId));
    }

    @GetMapping("/{id}/status-history")
    @Operation(summary = "Lấy lịch sử thay đổi trạng thái của lịch hẹn",
               description = "Trả về danh sách các thay đổi trạng thái của lịch hẹn, bao gồm thông tin người thay đổi, thời gian và lý do")
    public ResponseEntity<List<AppointmentAuditLog>> getAppointmentStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getAuditLogsForAppointment(id));
    }



    @PatchMapping("/detail/{detailId}/status")
    @Operation(summary = "Cập nhật trạng thái dịch vụ cụ thể",
               description = "Bác sĩ cập nhật trạng thái cho dịch vụ mà họ phụ trách. Appointment status sẽ được tự động tính lại theo quy tắc: có 1 detail IN_PROGRESS → Appointment IN_PROGRESS; tất cả WAITING_RESULT → Appointment WAITING_RESULT; tất cả COMPLETED → Appointment COMPLETED")
    public ResponseEntity<String> updateAppointmentDetailStatus(
            @PathVariable Long detailId,
            @RequestParam AppointmentStatus status) {
        appointmentService.updateAppointmentDetailStatus(detailId, status);
        return ResponseEntity.ok("Cập nhật trạng thái dịch vụ thành công");
    }
}