package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.ServiceFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.response.feedback.AverageRatingResponse;
import com.S_Health.GenderHealthCare.dto.response.feedback.ServiceFeedbackResponse;
import com.S_Health.GenderHealthCare.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@SecurityRequirement(name = "api")
public class ServiceFeedbackAPI {
    @Autowired
    FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ServiceFeedbackResponse> create(@RequestBody ServiceFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.createFeedback(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceFeedbackResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<ServiceFeedbackResponse>> getByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(feedbackService.getByAppointmentId(appointmentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceFeedbackResponse> update(@PathVariable Long id,
                                                          @RequestBody ServiceFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.update(id, request));
    }

    @GetMapping("average-rating/{serviceId}")
    public ResponseEntity<AverageRatingResponse> getAverageRatingByServiceId(@PathVariable Long serviceId) {
        return ResponseEntity.ok(feedbackService.getAverageRatingByServiceId(serviceId));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ServiceFeedbackResponse>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(feedbackService.getByServiceId(serviceId));
    }


}
