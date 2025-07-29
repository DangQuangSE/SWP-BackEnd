package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.ConsultantFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.response.feedback.ConsultantFeedbackResponse;
import com.S_Health.GenderHealthCare.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultant-feedbacks")
@SecurityRequirement(name = "api")
@Tag(name = "Consultant Feedback API", description = "API quản lý đánh giá bác sĩ")
public class ConsultantFeedbackAPI {

    @Autowired
    FeedbackService feedbackService;    

    @PostMapping
    public ResponseEntity<ConsultantFeedbackResponse> create(@RequestBody ConsultantFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.createConsultantFeedback(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultantFeedbackResponse> update(@PathVariable Long id,
                                                             @RequestBody ConsultantFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.updateConsultantFeedback(id, request));
    }

    @GetMapping("/service-feedback/{serviceFeedbackId}")
    public ResponseEntity<List<ConsultantFeedbackResponse>> getByServiceFeedback(@PathVariable Long serviceFeedbackId) {
        return ResponseEntity.ok(feedbackService.getByServiceFeedbackId(serviceFeedbackId));
    }

    @GetMapping("/consultant/{consultantId}")
    @Operation(summary = "Lấy danh sách feedback của bác sĩ",
               description = "Lấy tất cả feedback mà bác sĩ nhận được từ bệnh nhân")
    public ResponseEntity<List<ConsultantFeedbackResponse>> getByConsultant(@PathVariable Long consultantId) {
        return ResponseEntity.ok(feedbackService.getByConsultantId(consultantId));
    }
}
