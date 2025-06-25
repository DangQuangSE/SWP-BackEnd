package com.S_Health.GenderHealthCare.api;
import com.S_Health.GenderHealthCare.dto.AppointmentDTO;
import com.S_Health.GenderHealthCare.dto.request.appointment.UpdateAppointmentRequest;
import com.S_Health.GenderHealthCare.service.appointment.AppointmentService;
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
    @GetMapping("{id}")
    public ResponseEntity getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }
    //hiển thị cho bác sĩ theo status
    @GetMapping("/appointments/my-schedule")
    public ResponseEntity<List<AppointmentDTO>> getMySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsForDoctorOnDate(date);
        return ResponseEntity.ok(appointments);
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
    @PutMapping("/{id}/checkin")
    public ResponseEntity checkInAppointment(@PathVariable Long id){
        appointmentService.checkInAppointment(id);
        return ResponseEntity.noContent().build();
    }

}
