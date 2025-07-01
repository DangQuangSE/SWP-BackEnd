package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.dto.request.notification.NotificationRequest;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationAppointmentResponse;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationCycleTrackingResponse;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.CycleTracking;
import com.S_Health.GenderHealthCare.entity.Notification;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.NotificationType;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.CycleTrackingRepository;
import com.S_Health.GenderHealthCare.repository.NotificationRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    CycleTrackingRepository cycleTrackingRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Long userId = authUtil.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new AppException("Cuộc hẹn không tồn tại"));
        }

        CycleTracking cycleTracking = null;
        if (request.getCycleTrackingId() != null) {
            cycleTracking = cycleTrackingRepository.findById(request.getCycleTrackingId())
                    .orElseThrow(() -> new AppException("Không tìm thấy chu kỳ theo dõi"));
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .type(NotificationType.valueOf(request.getType()))
                .appointment(appointment)
                .cycleTracking(cycleTracking)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        return modelMapper.map(notification, NotificationResponse.class);
    }

    public List<NotificationResponse> getNotificationsByUser() {
        Long userId = authUtil.getCurrentUserId();
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> modelMapper.map(notification, NotificationResponse.class))
                .collect(Collectors.toList());
    }

    public NotificationResponse getNotificationById(Long notificationId) {
        Long userId = authUtil.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException("Thông báo không tồn tại"));
        return mapToResponse(notification);
    }


    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = authUtil.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException("Thông báo không tồn tại"));
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }


    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        for (Notification notification : notifications) {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        }
    }


    public Long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }


    @Transactional
    public void deleteNotification(Long notificationId) {
        Long userId = authUtil.getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new AppException("Thông báo không tồn tại"));
        notification.setIsActive(false);
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .type(notification.getType().name())
                .createdAt(notification.getCreatedAt())
                .appointment(notification.getAppointment() != null
                        ? NotificationAppointmentResponse.builder()
                        .id(notification.getAppointment().getId())
                        .doctorName(notification.getAppointment().getConsultant().getFullname())
                        .appointmentDate(notification.getAppointment().getCreated_at())
                        .build()
                        : null)
                .cycleTracking(notification.getCycleTracking() != null
                        ? NotificationCycleTrackingResponse.builder()
                        .id(notification.getCycleTracking().getId())
                        .cycleStartDate(notification.getCycleTracking().getStartDate())
//                        .duration(notification.getCycleTracking().)
                        .build()
                        : null)
                .build();
    }
}
