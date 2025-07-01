package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.request.notification.NotificationRequest;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationResponse;
import com.S_Health.GenderHealthCare.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "api")
public class NotificationAPI {
    @Autowired
    NotificationService notificationService;

    @PostMapping
    @Operation(
            summary = "Tạo notification",
            description = "Tạo notification mới cho user. Có thể gắn với appointment hoặc cycle tracking."
    )
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationRequest request) {
        return ResponseEntity.status(201).body(notificationService.createNotification(request));
    }

    // hoàn thành ở frondend
    @GetMapping
    @Operation(
            summary = "Lấy danh sách notification",
            description = "Lấy tất cả notification của user theo userId, sắp xếp theo createdAt DESC."
    )
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getNotificationsByUser());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết notification",
            description = "Lấy chi tiết thông tin một notification theo id và userId."
    )
    public ResponseEntity<NotificationResponse> getById(@PathVariable Long id,
                                                        @RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationById(id, userId));
    }

    @PatchMapping("/{id}/read")
    @Operation(
            summary = "Đánh dấu notification đã đọc",
            description = "Đánh dấu một notification của user đã được đọc."
    )
    public ResponseEntity<Void> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(
            summary = "Đánh dấu tất cả notification đã đọc",
            description = "Đánh dấu toàn bộ notification của user là đã đọc."
    )
    public ResponseEntity<Void> markAllRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Đếm số notification chưa đọc",
            description = "Trả về số lượng notification chưa đọc của user."
    )
    public ResponseEntity<Long> countUnread(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xóa notification",
            description = "Xóa một notification theo id và userId."
    )
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }
}
