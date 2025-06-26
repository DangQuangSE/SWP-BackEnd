package com.S_Health.GenderHealthCare.api.DashboardApi;

import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "api")
public class AdminDashboardAPI {
    @Autowired
    AppointmentService appointmentService;
    //Tổng quan hệ thống: số lượng users, lịch hẹn, lượt truy cập
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {
        return ResponseEntity.ok("Admin overview data");
    }

    //Thống kê nâng cao (feedback, xét nghiệm, hoạt động người dùng)
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok("Admin statistics");
    }

    @GetMapping("/user-activity")
    public ResponseEntity<?> getUserActivity() {
        return ResponseEntity.ok("User activity list");
    }

}
