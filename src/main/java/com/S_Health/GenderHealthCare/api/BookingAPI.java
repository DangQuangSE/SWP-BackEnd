package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.service.BookingRequest;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.service.medicalService.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/booking")
@SecurityRequirement(name = "api")
public class BookingAPI {
    @Autowired
    BookingService bookingService;
    @PostMapping("/medicalService")
    public ResponseEntity bookAppointment(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user
    ) {
        long customerId = user.getId(); // lấy ID từ token
        return ResponseEntity.ok(bookingService.bookingService(request, customerId));
    }
}
