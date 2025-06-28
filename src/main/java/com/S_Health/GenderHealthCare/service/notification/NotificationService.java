package com.S_Health.GenderHealthCare.service.notification;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.Notification;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.NotificationType;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.exception.exceptions.NotFoundException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.NotificationRepository;
import com.S_Health.GenderHealthCare.service.authentication.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Tạo thông báo mới
     */
    public Notification createNotification(Notification notification) {
        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }
        return notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo nhắc nhở lịch hẹn
     */
    public Notification createAppointmentReminder(Appointment appointment) {
        // Kiểm tra xem lịch hẹn có hợp lệ không
        if (appointment == null || appointment.getCustomer() == null) {
            throw new BadRequestException("Lịch hẹn không hợp lệ hoặc không có thông tin khách hàng");
        }

        Notification notification = new Notification();
        notification.setUser(appointment.getCustomer());
        notification.setAppointment(appointment);
        notification.setTitle("Nhắc nhở lịch hẹn");

        String formattedDate = appointment.getPreferredDate().format(DATE_FORMATTER);
        String serviceName = appointment.getService() != null ? appointment.getService().getName() : "chưa xác định";

        notification.setContent("Bạn có lịch hẹn vào ngày mai (" + formattedDate + 
                ") với dịch vụ " + serviceName + ". Vui lòng chuẩn bị đầy đủ thông tin cần thiết.");
        notification.setType(NotificationType.APPOINTMENT_REMINDER);
        notification.setIsRead(false);

        // Gửi email nhắc nhở
        emailService.sendAppointmentReminder(appointment);

        return notificationRepository.save(notification);
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông báo với ID: " + notificationId));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả thông báo của người dùng đã đọc
     */
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    /**
     * Lấy danh sách thông báo của người dùng
     */
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy số lượng thông báo chưa đọc của người dùng
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Tạo thông báo nhắc nhở cho tất cả lịch hẹn sắp tới trong ngày mai
     */
    public void createAppointmentRemindersForTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> tomorrowAppointments = appointmentRepository.findByPreferredDateAndStatusAndIsActiveTrue(
                tomorrow, AppointmentStatus.BOOKED);

        int count = 0;
        for (Appointment appointment : tomorrowAppointments) {
            // Kiểm tra xem đã gửi thông báo chưa
            if (!notificationRepository.existsByAppointmentAndType(
                    appointment, NotificationType.APPOINTMENT_REMINDER)) {
                createAppointmentReminder(appointment);
                count++;
            }
        }

        System.out.println("Đã tạo " + count + " thông báo nhắc nhở lịch hẹn (trong hệ thống và email) cho ngày " + tomorrow.format(DATE_FORMATTER));
    }

    /**
     * Tạo thông báo xác nhận đặt lịch hẹn
     */
    public Notification createAppointmentConfirmation(Appointment appointment) {
        Notification notification = new Notification();
        notification.setUser(appointment.getCustomer());
        notification.setAppointment(appointment);
        notification.setTitle("Xác nhận lịch hẹn");

        String formattedDate = appointment.getPreferredDate().format(DATE_FORMATTER);
        String serviceName = appointment.getService() != null ? appointment.getService().getName() : "chưa xác định";
        String consultantName = appointment.getConsultant() != null ? appointment.getConsultant().getFullname() : "chưa xác định";

        notification.setContent("Lịch hẹn của bạn vào ngày " + formattedDate + 
                " với dịch vụ " + serviceName + " đã được xác nhận. Tư vấn viên: " + consultantName);
        notification.setType(NotificationType.APPOINTMENT_CONFIRMATION);
        notification.setIsRead(false);

        // Gửi email xác nhận
        emailService.sendAppointmentConfirmation(appointment);

        return notificationRepository.save(notification);
    }

    /**
     * Tạo thông báo hủy lịch hẹn
     */
    public Notification createAppointmentCancellation(Appointment appointment, String reason) {
        Notification notification = new Notification();
        notification.setUser(appointment.getCustomer());
        notification.setAppointment(appointment);
        notification.setTitle("Hủy lịch hẹn");

        String formattedDate = appointment.getPreferredDate().format(DATE_FORMATTER);
        String serviceName = appointment.getService() != null ? appointment.getService().getName() : "chưa xác định";

        notification.setContent("Lịch hẹn của bạn vào ngày " + formattedDate + 
                " với dịch vụ " + serviceName + " đã bị hủy. " + 
                (reason != null && !reason.isEmpty() ? "Lý do: " + reason : ""));
        notification.setType(NotificationType.APPOINTMENT_CANCELLATION);
        notification.setIsRead(false);

        // Gửi email thông báo hủy lịch hẹn
        emailService.sendAppointmentCancellation(appointment, reason);

        return notificationRepository.save(notification);
    }
}
