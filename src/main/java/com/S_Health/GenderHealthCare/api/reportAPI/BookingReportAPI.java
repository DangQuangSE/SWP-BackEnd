package com.S_Health.GenderHealthCare.api.reportAPI;

import com.S_Health.GenderHealthCare.dto.response.report.BookingReportResponse;
import com.S_Health.GenderHealthCare.dto.response.report.ServiceBookingReportDTO;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/booking-reports")
public class BookingReportAPI {
    @Autowired
    AppointmentRepository appointmentRepository;

    @GetMapping("/stats")
    public ResponseEntity<List<ServiceBookingReportDTO>> getServiceBookingStats(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "service_id", required = false) Long serviceId
    ) {
        List<ServiceBookingReportDTO> report = appointmentRepository.getServiceBookingReport(startDate, endDate, serviceId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary")
    public ResponseEntity<BookingReportResponse> getServiceBookingSummary(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        BookingReportResponse summary = appointmentRepository.getBookingSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
