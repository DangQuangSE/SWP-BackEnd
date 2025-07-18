package com.S_Health.GenderHealthCare.NotificationTest;

import com.S_Health.GenderHealthCare.dto.request.notification.NotificationRequest;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationResponse;
import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.Notification;
import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.enums.NotificationType;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.repository.NotificationRepository;
import com.S_Health.GenderHealthCare.repository.UserRepository;
import com.S_Health.GenderHealthCare.service.NotificationService;
import com.S_Health.GenderHealthCare.service.authentication.EmailService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    AppointmentRepository appointmentRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(10L)
                .fullname("Học Viên")
                .email("hocvien@example.com")
                .build();

        notification = Notification.builder()
                .id(100L)
                .title("Thông báo học tập")
                .content("Nội dung thông báo")
                .user(user)
                .type(NotificationType.SYSTEM)
                .isRead(false)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("createNotification - thành công")
    void testCreateNotification_Success() {
        NotificationRequest request = NotificationRequest.builder()
                .title("Thông báo học tập")
                .content("Đây là nội dung")
                .type("SYSTEM")
                .build();

        when(authUtil.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any())).thenReturn(notification);
        when(modelMapper.map(any(), eq(NotificationResponse.class)))
                .thenReturn(NotificationResponse.builder().id(110L).title("Thông báo học tập").build());

        NotificationResponse result = notificationService.createNotification(request);

        assertNotNull(result);
        assertEquals(110L, result.getId());
        assertEquals("Thông báo học tập", result.getTitle());
    }

    @Test
    @DisplayName("createNotification - user không tồn tại")
    void testCreateNotification_UserNotFound() {
        NotificationRequest request = NotificationRequest.builder()
                .title("Test")
                .content("Nội dung")
                .type("SYSTEM")
                .build();

        when(authUtil.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> notificationService.createNotification(request));
        assertEquals("Không tìm thấy người dùng", exception.getMessage());
    }

    @Test
    @DisplayName("getNotificationsByUser - trả về danh sách")
    void testGetNotificationsByUser() {
        when(authUtil.getCurrentUserId()).thenReturn(user.getId());
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));
        when(modelMapper.map(any(), eq(NotificationResponse.class)))
                .thenReturn(
                        NotificationResponse.builder().id(notification.getId()).title(notification.getTitle()).build());

        var results = notificationService.getNotificationsByUser();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(notification.getId(), results.get(0).getId());
        assertEquals(notification.getTitle(), results.get(0).getTitle());
    }

    @Test
    @DisplayName("deleteNotification - thành công")
    void testDeleteNotification_Success() {
        when(authUtil.getCurrentUserId()).thenReturn(user.getId());
        when(notificationRepository.findByIdAndUserId(notification.getId(), user.getId()))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notification.getId());

        assertFalse(notification.getIsActive());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead - unread to read")
    void testMarkAsRead_Success() {
        when(authUtil.getCurrentUserId()).thenReturn(user.getId());
        when(notificationRepository.findByIdAndUserId(notification.getId(), user.getId()))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getId());

        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead - already read")
    void testMarkAsRead_AlreadyRead() {
        notification.setIsRead(true);
        when(authUtil.getCurrentUserId()).thenReturn(user.getId());
        when(notificationRepository.findByIdAndUserId(notification.getId(), user.getId()))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(notification.getId());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getNotificationById - không tìm thấy")
    void testGetNotificationById_NotFound() {
        when(authUtil.getCurrentUserId()).thenReturn(user.getId());
        when(notificationRepository.findByIdAndUserId(999L, user.getId())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> notificationService.getNotificationById(999L));
        assertEquals("Thông báo không tồn tại", exception.getMessage());
    }

    @Test
    @DisplayName("countUnread - đếm số chưa đọc")
    void testCountUnread() {
        when(notificationRepository.countByUserIdAndIsReadFalse(user.getId())).thenReturn(3L);
        long count = notificationService.countUnread(user.getId());
        assertEquals(3L, count);
    }

    @Test
    @DisplayName("markAllAsRead - có thông báo chưa đọc")
    void testMarkAllAsRead_SomeUnread() {
        Notification n1 = Notification.builder().id(1L).isRead(false).build();
        Notification n2 = Notification.builder().id(2L).isRead(true).build();

        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(user.getId());
        verify(notificationRepository).save(n1);
        verify(notificationRepository, never()).save(n2);
    }

    @Test
    @DisplayName("sendReminders - gửi email thành công")
    void testSendReminders_Success() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Appointment appt = new Appointment();
        appt.setPreferredDate(tomorrow);
        User customer = new User();
        customer.setFullname("Học viên A");
        customer.setEmail("a@example.com");
        appt.setCustomer(customer);

        com.S_Health.GenderHealthCare.entity.Service service = new com.S_Health.GenderHealthCare.entity.Service();
        service.setName("Tư vấn sức khỏe");
        appt.setService(service);

        when(appointmentRepository.findByPreferredDateAndIsActiveTrue(tomorrow)).thenReturn(List.of(appt));

        notificationService.sendReminders();

        verify(emailService).sendAppointmentReminder(eq("a@example.com"), eq(tomorrow));
    }
}
