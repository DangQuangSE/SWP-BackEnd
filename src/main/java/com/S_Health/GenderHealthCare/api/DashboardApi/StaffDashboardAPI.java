package com.S_Health.GenderHealthCare.api.DashboardApi;

import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.AppointmentStatsDTO;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@SecurityRequirement(name = "api")
public class StaffDashboardAPI {
    @Autowired
    AppointmentService appointmentService;


    @GetMapping("/appointments")
    @Operation(summary = "Lấy danh sách lịch hẹn")
    public List<AppointmentDTO> getAppointments(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long doctorId,
                                                @RequestParam(required = false) LocalDate date) {
        return appointmentService.getStaffAppointments(status, doctorId, date);
    }

    @PutMapping("/appointments/{id}/checkin")
    public ResponseEntity<?> checkInAppointment(@PathVariable Long id) {
        appointmentService.checkInAppointment(id);
        return ResponseEntity.ok("Check-in thành công");
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Đã hủy lịch hẹn");
    }


    @GetMapping("/appointments/{id}")
    @Operation(summary = "Xem chi tiết lịch hẹn")
    public AppointmentDTO getAppointmentDetail(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }


    @GetMapping("/appointments/statistics")
    @Operation(summary = "Thống kê tổng quan")
    public AppointmentStatsDTO getStatistics() {
        return appointmentService.getAppointmentStats();
    }

    @GetMapping("/appointments")
    @Operation(summary = "Danh sách khách của một bác sĩ sau giờ check-in")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }



    }
