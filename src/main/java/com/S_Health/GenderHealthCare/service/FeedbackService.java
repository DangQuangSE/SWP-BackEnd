package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.ConsultantFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.request.ServiceFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.response.ConsultantFeedbackResponse;
import com.S_Health.GenderHealthCare.dto.response.ServiceFeedbackResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.ConsultantFeedback;
import com.S_Health.GenderHealthCare.entity.ServiceFeedback;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.ConsultantFeedbackRepository;
import com.S_Health.GenderHealthCare.repository.ServiceFeedbackRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    ServiceFeedbackRepository serviceFeedbackRepository;
    @Autowired
    ConsultantFeedbackRepository consultantFeedbackRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthUtil authUtil;

    public ServiceFeedbackResponse createFeedback(ServiceFeedbackRequest request){
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Cuộc hẹn không tồn tại"));

        Long userId = authUtil.getCurrentUserId();
        Long customerId = appointment.getCustomer().getId();

        if (!customerId.equals(userId)) {
            throw new BadRequestException("Bạn chưa có cuộc hẹn nào");
        }

        ServiceFeedback serviceFeedback = ServiceFeedback.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .createAt(LocalDateTime.now())
                .appointment(appointment)
                .build();
        serviceFeedbackRepository.save(serviceFeedback);

        return ServiceFeedbackResponse.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(serviceFeedback.getCreateAt())
                .appointmentId(request.getAppointmentId())
                .build();
    }

    public ServiceFeedbackResponse getById(Long id) {
        ServiceFeedback feedback = serviceFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy feedback này"));

        return ServiceFeedbackResponse.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreateAt())
                .appointmentId(feedback.getAppointment().getId())
                .consultantFeedbacks(List.of())
                .build();
    }

    public List<ServiceFeedbackResponse> getByAppointmentId(Long appointmentId) {
        return serviceFeedbackRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(feedback -> ServiceFeedbackResponse.builder()
                        .id(feedback.getId())
                        .rating(feedback.getRating())
                        .comment(feedback.getComment())
                        .createdAt(feedback.getCreateAt())
                        .appointmentId(feedback.getAppointment().getId())
                        .consultantFeedbacks(List.of())
                        .build())
                .collect(Collectors.toList());
    }

    public ServiceFeedbackResponse update(Long id, ServiceFeedbackRequest request) {
        ServiceFeedback feedback = serviceFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy feedback này"));

        Long userId = authUtil.getCurrentUserId();

        Long appointmentUserId = feedback.getAppointment().getCustomer().getId();
        if (!appointmentUserId.equals(userId)) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback.setUpdateAt(LocalDateTime.now());
        ServiceFeedback updated = serviceFeedbackRepository.save(feedback);

        return ServiceFeedbackResponse.builder()
                .id(updated.getId())
                .rating(updated.getRating())
                .comment(updated.getComment())
                .createdAt(updated.getCreateAt())
                .updateAt(updated.getUpdateAt())
                .appointmentId(updated.getAppointment().getId())
                .consultantFeedbacks(List.of())
                .build();
    }

    public ConsultantFeedbackResponse createConsultantFeedback(ConsultantFeedbackRequest request){
        ServiceFeedback feedback = serviceFeedbackRepository.findById(request.getServiceFeedbackId())
                .orElseThrow(() -> new BadRequestException("Chưa có đánh giá cho cuộc hẹn này"));

        Long userId = authUtil.getCurrentUserId();

        Long customerId = feedback.getAppointment().getCustomer().getId();

        if (!customerId.equals(userId)) {
            throw new AuthenticationException("Bạn chưa có cuộc hẹn nào");
        }

        User consultant = userRepository.findByIdAndRole(request.getConsultantId(), UserRole.CONSULTANT)
                .orElseThrow(() -> new BadRequestException("Không có bác sĩ này"));

        Long consultantId = feedback.getAppointment().getConsultant().getId();
        if (!consultantId.equals(request.getConsultantId())) {
            throw new RuntimeException("Bác sĩ không thuộc cuộc hẹn này");
        }


        ConsultantFeedback consultantFeedback = ConsultantFeedback.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .consultantId(request.getConsultantId())
                .serviceFeedback(feedback)
                .createAt(LocalDateTime.now())
                .build();
        consultantFeedbackRepository.save(consultantFeedback);

        return ConsultantFeedbackResponse.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(consultantFeedback.getCreateAt())
                .consultantId(request.getConsultantId())
                .build();
    }

    public ConsultantFeedbackResponse updateConsultantFeedback(Long id, ConsultantFeedbackRequest request) {
        ConsultantFeedback cf = consultantFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("không có đánh giá bác sĩ nào"));

        Long userId = authUtil.getCurrentUserId();

        Long appointmentUserId = cf.getServiceFeedback().getAppointment().getCustomer().getId();
        if (!appointmentUserId.equals(userId)) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này");
        }

        User consultant = userRepository.findByIdAndRole(request.getConsultantId(), UserRole.CONSULTANT)
                .orElseThrow(() -> new BadRequestException("Không có bác sĩ này"));

        Long consultantId = cf.getServiceFeedback().getAppointment().getConsultant().getId();
        if (!consultantId.equals(request.getConsultantId())) {
            throw new RuntimeException("Bác sĩ không thuộc cuộc hẹn này");
        }

        cf.setComment(request.getComment());
        cf.setRating(request.getRating());
        cf.setUpdateAt(LocalDateTime.now());
        ConsultantFeedback updated = consultantFeedbackRepository.save(cf);

        return ConsultantFeedbackResponse.builder()
                .id(updated.getId())
                .consultantId(updated.getConsultantId())
                .comment(updated.getComment())
                .createdAt(updated.getCreateAt())
                .updateAt(updated.getUpdateAt())
                .build();
    }

    public List<ConsultantFeedbackResponse> getByServiceFeedbackId(Long feedbackId) {
        return consultantFeedbackRepository.findByServiceFeedbackId(feedbackId)
                .stream()
                .map(cf -> ConsultantFeedbackResponse.builder()
                        .id(cf.getId())
                        .consultantId(cf.getId())
                        .comment(cf.getComment())
                        .createdAt(cf.getCreateAt())
                        .build())
                .collect(Collectors.toList());
    }




}
