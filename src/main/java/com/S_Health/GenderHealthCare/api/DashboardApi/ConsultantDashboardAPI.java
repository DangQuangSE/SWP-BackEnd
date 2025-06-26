package com.S_Health.GenderHealthCare.api.DashboardApi;

import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultant")
@SecurityRequirement(name = "api")
public class ConsultantDashboardAPI {
    @Autowired
    AppointmentService appointmentService;

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments() {
        return ResponseEntity.ok("Lịch hẹn tư vấn của tôi");
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<?> getClientProfile(@PathVariable Long id) {
        return ResponseEntity.ok("Hồ sơ sức khỏe khách hàng " + id);
    }
}
