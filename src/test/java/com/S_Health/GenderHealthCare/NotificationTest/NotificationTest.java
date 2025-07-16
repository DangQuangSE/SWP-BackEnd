package com.S_Health.GenderHealthCare.NotificationTest;

import com.S_Health.GenderHealthCare.api.NotificationAPI;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationAppointmentResponse;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationCycleTrackingResponse;
import com.S_Health.GenderHealthCare.dto.response.nofitication.NotificationResponse;
import com.S_Health.GenderHealthCare.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(NotificationAPI.class)
@Import(NotificationTest.TestConfig.class)
public class NotificationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<NotificationResponse> notifications;

    @BeforeEach
    void setUp() {
        NotificationAppointmentResponse appointment = NotificationAppointmentResponse.builder()
                .id(100L)
                .doctorName("doctor-1")
                .build();

        NotificationCycleTrackingResponse cycleTracking = NotificationCycleTrackingResponse.builder()
                .id(200L)
                .cycleStartDate(LocalDate.now())
                .duration(28)
                .build();

        NotificationResponse notif1 = NotificationResponse.builder()
                .id(1L)
                .title("Notification 1")
                .content("Content 1")
                .isRead(false)
                .isActive(true)
                .type("REMINDER")
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .appointment(appointment)
                .cycleTracking(cycleTracking)
                .build();

        NotificationResponse notif2 = NotificationResponse.builder()
                .id(2L)
                .title("Notification 2")
                .content("Content 2")
                .isRead(true)
                .isActive(true)
                .type("SYSTEM")
                .createdAt(LocalDateTime.now().minusDays(1))
                .readAt(LocalDateTime.now().minusDays(1))
                .appointment(null)
                .cycleTracking(null)
                .build();

        notifications = List.of(notif1, notif2);
    }

    @Test
    @DisplayName("TC01 - Lấy danh sách notification thành công")
    void getAllNotifications_success() throws Exception {
        // Arrange
        Mockito.when(notificationService.getNotificationsByUser())
                .thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/notifications")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Notification 1"))
                .andExpect(jsonPath("$[0].appointment.id").value(100))
                .andExpect(jsonPath("$[0].cycleTracking.id").value(200))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Notification 2"));
    }
    @Test
    @DisplayName("TC02 - Không có notification trả về empty list")
    void getAllNotifications_emptyList() throws Exception {
        Mockito.when(notificationService.getNotificationsByUser())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/notifications")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("TC03 - Service ném exception trả về 500")
    void getAllNotifications_serviceThrowsException() throws Exception {
        Mockito.when(notificationService.getNotificationsByUser())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/notifications")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("TC04 - Notifications được sắp xếp theo createdAt DESC")
    void getAllNotifications_sortedByCreatedAtDesc() throws Exception {
        NotificationResponse notifNew = NotificationResponse.builder()
                .id(10L)
                .title("Newest Notification")
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .isActive(true)
                .type("REMINDER")
                .build();

        NotificationResponse notifOld = NotificationResponse.builder()
                .id(20L)
                .title("Older Notification")
                .createdAt(LocalDateTime.now().minusDays(5))
                .isRead(true)
                .isActive(true)
                .type("SYSTEM")
                .build();

        Mockito.when(notificationService.getNotificationsByUser())
                .thenReturn(List.of(notifNew, notifOld));

        mockMvc.perform(get("/api/notifications")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(20));
    }

    @Test
    @DisplayName("TC05 - Trả về notification với field null không lỗi")
    void getAllNotifications_withNullFields() throws Exception {
        NotificationResponse notif = NotificationResponse.builder()
                .id(99L)
                .title(null)
                .content(null)
                .isRead(null)
                .isActive(true)
                .type(null)
                .createdAt(null)
                .readAt(null)
                .appointment(null)
                .cycleTracking(null)
                .build();

        Mockito.when(notificationService.getNotificationsByUser())
                .thenReturn(List.of(notif));

        mockMvc.perform(get("/api/notifications")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));
    }

    /**
     * Cấu hình bean giả thay thế @MockBean
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        NotificationService notificationService() {
            return Mockito.mock(NotificationService.class);
        }
    }
}
