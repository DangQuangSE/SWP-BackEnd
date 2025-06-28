package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.NotificationDTO;
import com.S_Health.GenderHealthCare.entity.Notification;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.service.notification.NotificationService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "api")
@Tag(name = "Thông báo", description = "API quản lý thông báo")
public class NotificationAPI {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo", 
               description = "Trả về danh sách thông báo của người dùng hiện tại")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        User currentUser = authUtil.getCurrentUser();
        List<Notification> notifications = notificationService.getNotificationsByUser(currentUser);
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDTOs);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc", 
               description = "Trả về số lượng thông báo chưa đọc của người dùng hiện tại")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        User currentUser = authUtil.getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc", 
               description = "Đánh dấu một thông báo là đã đọc")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(convertToDTO(notification));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc", 
               description = "Đánh dấu tất cả thông báo của người dùng hiện tại là đã đọc")
    public ResponseEntity<Map<String, String>> markAllNotificationsAsRead() {
        User currentUser = authUtil.getCurrentUser();
        notificationService.markAllAsRead(currentUser);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Tất cả thông báo đã được đánh dấu là đã đọc");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-reminder")
    @Operation(summary = "Tạo thông báo nhắc nhở thử nghiệm", 
               description = "API này chỉ dùng để kiểm tra chức năng tạo thông báo nhắc nhở (Chỉ admin mới có quyền)")
    public ResponseEntity<Map<String, String>> testReminderGeneration() {
        User currentUser = authUtil.getCurrentUser();
        if (!currentUser.getRole().toString().equals("ADMIN")) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Bạn không có quyền truy cập chức năng này");
            return ResponseEntity.status(403).body(errorResponse);
        }

        notificationService.createAppointmentRemindersForTomorrow();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã tạo thông báo nhắc nhở thử nghiệm");
        return ResponseEntity.ok(response);
    }

    /**
     * Chuyển đổi từ entity sang DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());

        if (notification.getAppointment() != null) {
            dto.setAppointmentId(notification.getAppointment().getId());
        }

        if (notification.getCycleTracking() != null) {
            dto.setCycleTrackingId(notification.getCycleTracking().getId());
        }

        return dto;
    }
}
