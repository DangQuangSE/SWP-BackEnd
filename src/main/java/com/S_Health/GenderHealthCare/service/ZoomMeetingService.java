package com.S_Health.GenderHealthCare.service;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.entity.AppointmentDetail;
import com.S_Health.GenderHealthCare.enums.AppointmentStatus;
import com.S_Health.GenderHealthCare.enums.ServiceType;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.AppointmentDetailRepository;
import com.S_Health.GenderHealthCare.repository.AppointmentRepository;
import com.S_Health.GenderHealthCare.service.authentication.EmailService;
import com.S_Health.GenderHealthCare.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ZoomMeetingService {
    @Autowired
    ZoomOAuthService zoomOAuthService;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    AuthUtil authUtil;
    @Autowired
    EmailService emailService;
    @Autowired
    AppointmentDetailRepository appointmentDetailRepository;

    public Map<String, String> createMeeting(Long appointmentId) {
        // Gọi method bạn vừa tạo

        //check id
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("Cuộc hẹn không tồn tại"));
        //check status
        if (!appointment.getStatus().equals(AppointmentStatus.CONFIRMED) &&
        !appointment.getService().getType().equals(ServiceType.CONSULTING_ON)) {
            throw new AppException("Cuộc hẹn chưa được xác nhận hoặc không phải cuộc hẹn tư vấn trực tuyến");
        }

        Long userId = authUtil.getCurrentUserId();

        if(!appointment.getCustomer().equals(userId)) {
            throw new AppException(" Bạn không có trong cuộc họp này");
        }

        String accessToken = zoomOAuthService.getAccessToken();

        String topic = appointment.getService().getName();
        String startTime = appointment.getServiceSlotPool().getStartTime().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("topic", topic);
        body.put("type", 2);
        body.put("start_time", startTime);
        body.put("duration", 90);// Thời gian cuộc họp (phút)
        body.put("timezone", "Asia/Ho_Chi_Minh");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.zoom.us/v2/users/me/meetings",
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        System.out.println("Zoom Response = " + responseBody);

        // Lấy start_url và join_url từ response
        String joinUrl = (String) responseBody.get("join_url");
        String startUrl = (String) responseBody.get("start_url");
        // id cho việc xóa cuộc hẹn nếu cần
//        Integer meetingId = (Integer) responseBody.get("id");
        if(joinUrl == null || startUrl == null) {
            throw new AppException("Không thể tạo cuộc họp Zoom, vui lòng thử lại sau");
        }else {
            AppointmentDetail appointmentDetail = appointmentDetailRepository.findByAppointmentId(appointmentId)
                    .orElseThrow(() -> new AppException("Không tìm thấy chi tiết cuộc hẹn"));
            appointmentDetail.setJoinUrl(joinUrl);
            appointmentDetail.setStartUrl(startUrl);
            appointmentDetailRepository.save(appointmentDetail);

            String emailCustomer = appointment.getCustomer().getEmail();
            String serviceName = appointment.getService().getName();
            emailService.sendUrlCurtomerZoom(emailCustomer,startTime,joinUrl,serviceName);

            String emailConsultant = appointment.getConsultant().getEmail();
            emailService.sendUrlConsultantZoom(emailConsultant,startTime,startUrl,serviceName);
        }

        // Trả cả hai URL ra ngoài
        Map<String, String> result = new HashMap<>();
        result.put("join_url", joinUrl);
        result.put("start_url", startUrl);
        // id cho việc xóa cuộc hẹn nếu cần
//        result.put("meeting_id", String.valueOf(meetingId));

        return result;
    }
}
