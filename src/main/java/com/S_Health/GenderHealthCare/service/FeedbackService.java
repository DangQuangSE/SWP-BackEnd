package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.ConsultantFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.request.ServiceFeedbackRequest;
import com.S_Health.GenderHealthCare.dto.response.feedback.AverageRatingResponse;
import com.S_Health.GenderHealthCare.dto.response.feedback.ConsultantFeedbackResponse;
import com.S_Health.GenderHealthCare.dto.response.feedback.ServiceFeedbackResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.ConsultantFeedback;
import com.S_Health.GenderHealthCare.entity.ServiceFeedback;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.UserRole;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.ConsultantFeedbackRepository;
import com.S_Health.GenderHealthCare.repository.ServiceFeedbackRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

        // Kiểm tra null safety
        if (appointment.getCustomer() == null) {
            throw new AppException("Thông tin khách hàng không hợp lệ");
        }
        Long customerId = appointment.getCustomer().getId();

        if (!customerId.equals(userId)) {
            throw new AppException("Bạn chưa có cuộc hẹn nào");
        }
        ServiceFeedback serviceFeedback = ServiceFeedback.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .createAt(LocalDateTime.now())
                .appointment(appointment)
                .build();
        serviceFeedbackRepository.save(serviceFeedback);

        // Tạo consultant feedback cho tất cả consultant trong appointmentDetails
        List<ConsultantFeedbackResponse> consultantFeedbacks = new ArrayList<>();

        if (appointment.getAppointmentDetails() != null && !appointment.getAppointmentDetails().isEmpty()) {
            // Lấy danh sách unique consultant IDs từ appointmentDetails
            Set<Long> consultantIds = appointment.getAppointmentDetails().stream()
                    .filter(detail -> detail.getConsultant() != null)
                    .map(detail -> detail.getConsultant().getId())
                    .collect(Collectors.toSet());

            // Tạo consultant feedback cho mỗi consultant
            for (Long consultantId : consultantIds) {
                if (request.getCommentConsultant() != null && !request.getCommentConsultant().trim().isEmpty()) {
                    ConsultantFeedback consultantFeedback = ConsultantFeedback.builder()
                            .rating(request.getRating())
                            .comment(request.getCommentConsultant())
                            .consultantId(consultantId)
                            .serviceFeedback(serviceFeedback)
                            .createAt(serviceFeedback.getCreateAt())
                            .build();
                    consultantFeedbackRepository.save(consultantFeedback);

                    consultantFeedbacks.add(
                            ConsultantFeedbackResponse.builder()
                                    .id(consultantFeedback.getId())
                                    .rating(consultantFeedback.getRating())
                                    .comment(consultantFeedback.getComment())
                                    .createdAt(consultantFeedback.getCreateAt())
                                    .consultantId(consultantFeedback.getConsultantId())
                                    .build()
                    );
                }
            }
        }

        return ServiceFeedbackResponse.builder()
                .id(serviceFeedback.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(serviceFeedback.getCreateAt())
                .appointmentId(request.getAppointmentId())
                .consultantFeedbacks(consultantFeedbacks)
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
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("Cuộc hẹn không tồn tại"));

        ServiceFeedback  serviceFeedback = serviceFeedbackRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppException("Chưa có đánh giá cho cuộc hẹn này"));

        List<ConsultantFeedbackResponse> consultantFeedbacks = consultantFeedbackRepository.findByServiceFeedbackId(serviceFeedback.getId())
                .stream()
                .map(cf -> ConsultantFeedbackResponse.builder()
                        .id(cf.getId())
                        .rating(cf.getRating())
                        .comment(cf.getComment())
                        .createdAt(cf.getCreateAt())
                        .consultantId(cf.getConsultantId())
                        .build())
                .collect(Collectors.toList());

        return List.of(ServiceFeedbackResponse.builder()
                .id(serviceFeedback.getId())
                .rating(serviceFeedback.getRating())
                .comment(serviceFeedback.getComment())
                .createdAt(serviceFeedback.getCreateAt())
                .appointmentId(serviceFeedback.getAppointment().getId())
                .consultantFeedbacks(consultantFeedbacks)
                .build());
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

        List<ConsultantFeedback> consultantFeedbacks = consultantFeedbackRepository.findByServiceFeedbackId(feedback.getId());
        if (consultantFeedbacks.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đánh giá bác sĩ");
        }
        ConsultantFeedback consultantFeedback = consultantFeedbacks.get(0);

        consultantFeedback.setComment(request.getCommentConsultant());
        consultantFeedback.setRating(request.getRating());
        consultantFeedback.setUpdateAt(LocalDateTime.now());
        consultantFeedbackRepository.save(consultantFeedback);

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
                .orElseThrow(() -> new AppException("Chưa có đánh giá cho cuộc hẹn này"));

        Long userId = authUtil.getCurrentUserId();

        Long customerId = feedback.getAppointment().getCustomer().getId();

        if (!customerId.equals(userId)) {
            throw new AppException("Bạn chưa có cuộc hẹn nào");
        }

        User consultant = userRepository.findByIdAndRole(request.getConsultantId(), UserRole.CONSULTANT)
                .orElseThrow(() -> new AppException("Không có bác sĩ này"));

        // Kiểm tra consultant có thuộc appointment này không (qua appointmentDetails)
        boolean consultantBelongsToAppointment = feedback.getAppointment().getAppointmentDetails().stream()
                .anyMatch(detail -> detail.getConsultant() != null &&
                         detail.getConsultant().getId() == request.getConsultantId());

        if (!consultantBelongsToAppointment) {
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
                .orElseThrow(() -> new AppException("Không có bác sĩ này"));

        // Kiểm tra consultant có thuộc appointment này không (qua appointmentDetails)
        boolean consultantBelongsToAppointment = cf.getServiceFeedback().getAppointment().getAppointmentDetails().stream()
                .anyMatch(detail -> detail.getConsultant() != null &&
                         detail.getConsultant().getId() == request.getConsultantId());

        if (!consultantBelongsToAppointment) {
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
                        .rating(cf.getRating())
                        .consultantId(cf.getConsultantId())
                        .comment(cf.getComment())
                        .createdAt(cf.getCreateAt())
                        .build())
                .collect(Collectors.toList());
    }

    public AverageRatingResponse getAverageRatingByServiceId(Long serviceId) {
        List<Appointment> appointment = appointmentRepository.findByServiceIdAndIsRatedTrue(serviceId);

        if (appointment.isEmpty()) {
            return AverageRatingResponse.builder()
                    .serviceId(serviceId)
                    .averageRating(0.0)
                    .totalAppointment(0L)
                    .build();
            }

        List<Long> appointmentIds = appointment
                .stream()
                .map(Appointment::getId)
                .collect(Collectors.toList());

        List<ServiceFeedback> feedbacks = serviceFeedbackRepository.findByAppointmentIdIn(appointmentIds);

        if (feedbacks.isEmpty()) {
            return AverageRatingResponse.builder()
                    .serviceId(serviceId)
                    .averageRating(0.0)
                    .totalAppointment(0L)
                    .build();
            }


        double sumRate = feedbacks.stream()
                .mapToDouble(ServiceFeedback::getRating)
                .sum();

        double averageRating = sumRate / feedbacks.size();

        return AverageRatingResponse.builder()
                .serviceId(serviceId)
                .averageRating(averageRating)
                .totalAppointment((long) feedbacks.size())
                .build();
    }

    public List<ServiceFeedbackResponse> getByServiceId(Long serviceId) {
        List<Appointment> appointment = appointmentRepository.findByServiceIdAndIsRatedTrue(serviceId);
        // liệt kê tất cả id appointment của 1 service
        List<Long> appointmentIds = appointment
                .stream()
                .map(Appointment::getId)
                .collect(Collectors.toList());

        List<ServiceFeedback> feedbacks = serviceFeedbackRepository.findByAppointmentIdIn(appointmentIds);
        // liệt kê tất cả id serviceFeedback trong các appointment của service
        List<Long> serviceFeedbackIds =feedbacks
                .stream()
                .map(ServiceFeedback::getId)
                .collect(Collectors.toList());

        Map<Long, List<ConsultantFeedbackResponse>> consultantFeedbackMap =
                consultantFeedbackRepository.findByServiceFeedbackIdIn(serviceFeedbackIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                cf -> cf.getServiceFeedback().getId(),
                                Collectors.mapping(cf -> ConsultantFeedbackResponse.builder()
                                        .id(cf.getId())
                                        .rating(cf.getRating())
                                        .comment(cf.getComment())
                                        .createdAt(cf.getCreateAt())
                                        .consultantId(cf.getConsultantId())
                                        .updateAt(cf.getUpdateAt())
                                        .build(), Collectors.toList())
                        ));

        return feedbacks.stream()
                .map(feedback -> {
                    List<ConsultantFeedbackResponse> consultantFeedbacksOfThisFeedback =
                            consultantFeedbackMap.getOrDefault(feedback.getId(), Collections.emptyList());

                    return ServiceFeedbackResponse.builder()
                            .id(feedback.getId())
                            .rating(feedback.getRating())
                            .comment(feedback.getComment())
                            .createdAt(feedback.getCreateAt())
                            .appointmentId(feedback.getAppointment().getId())
                            .consultantFeedbacks(consultantFeedbacksOfThisFeedback)
                            .build();
                })
                .collect(Collectors.toList());
    }

}
