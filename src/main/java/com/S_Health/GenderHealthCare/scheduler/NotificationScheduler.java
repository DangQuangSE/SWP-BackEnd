package com.S_Health.GenderHealthCare.scheduler;

import com.S_Health.GenderHealthCare.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationScheduler {
    @Autowired
    private NotificationService notificationService;

    // Chạy mỗi ngày vào lúc 8:00 sáng
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendAppointmentReminders() {
        System.out.println("Bắt đầu gửi thông báo nhắc nhở lịch hẹn lúc " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
        notificationService.createAppointmentRemindersForTomorrow();
    }
}
