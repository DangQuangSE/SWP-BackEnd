package com.S_Health.GenderHealthCare.service.authentication;

import com.S_Health.GenderHealthCare.entity.Appointment;
import com.S_Health.GenderHealthCare.enums.UserRole;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    public void sendOtp(String toEmail, String otp) {
        try{
            Context context = new Context();

            context.setVariable("name", toEmail);
            context.setVariable("otp", otp);
            context.setVariable("messageLine1", "Bạn vừa yêu cầu xác minh tài khoản. Đây là mã OTP của bạn:");

            String html = templateEngine.process("emailotp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã xác minh đăng ký");
            helper.setText(html, true); // true = nội dung HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Lỗi gửi email: " + e.getMessage());
        }
    }

    public void sendForgotPasswordOtp(String toEmail, String otp) {
        try{
            Context context = new Context();

            context.setVariable("name", toEmail);
            context.setVariable("otp", otp);
            context.setVariable("messageLine1", "Bạn đã yêu cầu khôi phục mật khẩu. Đây là mã OTP của bạn:");

            String html = templateEngine.process("emailotp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã xác minh khôi phục mật khẩu");
            helper.setText(html, true); // true = nội dung HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Lỗi gửi email quên mật khẩu : " + e.getMessage());
        }
    }

    public void sendWelcome(String toEmail) {
        try{

            Context context = new Context();
            context.setVariable("name", toEmail);

            String html = templateEngine.process("emailwelcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Chào mừng bạn đến với S-HealthCare");
            helper.setText(html, true);

            mailSender.send(message);
        }catch (Exception e){
            System.out.println("Lỗi gửi email chào mừng: " + e.getMessage());
        }
    }

    public void sendWelcomeWithCredentials(String email, String randomPassword, UserRole role) {
        try {
            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("password", randomPassword);
            context.setVariable("role", role.name());

            String html = templateEngine.process("welcome-credentials", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Thông tin tài khoản " + role.name() + " - S-HealthCare");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Lỗi gửi email: " + e.getMessage());
        }

    }

    public void sendUrlCurtomerZoom (String toEmail, String startTime, String joinUrl, String serviceName) {
        try{

            Context context = new Context();
            context.setVariable("name", toEmail);
            context.setVariable("startTime", startTime);
            context.setVariable("joinUrl", joinUrl);
            context.setVariable("serviceName", serviceName);

            String html = templateEngine.process("emailZoom", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[SHeathCare] Thông tin buổi tư vấn của bạn");
            helper.setText(html, true);

            mailSender.send(message);
        }catch (Exception e){
            System.out.println("Lỗi gửi email Zoom cho khách hàng: " + e.getMessage());
        }
    }

    public void sendUrlConsultantZoom (String toEmail, String startTime, String joinUrl, String serviceName) {
        try{

            Context context = new Context();
            context.setVariable("name", toEmail);
            context.setVariable("startTime", startTime);
            context.setVariable("joinUrl", joinUrl);
            context.setVariable("serviceName", serviceName);

            String html = templateEngine.process("ConsultantZoom", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[SHeathCare] Thông tin buổi tư vấn của bạn");
            helper.setText(html, true);

            mailSender.send(message);
        }catch (Exception e){
            System.out.println("Lỗi gửi email Zoom cho bác sĩ: " + e.getMessage());
        }
    }

    /**
     * Gửi email nhắc nhở lịch hẹn
     */
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            if (appointment == null || appointment.getCustomer() == null || 
                appointment.getCustomer().getEmail() == null) {
                System.out.println("Không thể gửi email nhắc nhở: Thiếu thông tin lịch hẹn hoặc email khách hàng");
                return;
            }

            String customerEmail = appointment.getCustomer().getEmail();
            String customerName = appointment.getCustomer().getFullname();
            String serviceName = appointment.getService() != null ? 
                    appointment.getService().getName() : "Chưa xác định";

            LocalDate appointmentDate = appointment.getPreferredDate();
            LocalTime appointmentTime = appointment.getServiceSlotPool() != null ? 
                    appointment.getServiceSlotPool().getStartTime() : LocalTime.of(0, 0);

            String consultantName = appointment.getConsultant() != null ? 
                    appointment.getConsultant().getFullname() : "Chưa xác định";

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("serviceName", serviceName);
            context.setVariable("appointmentDate", appointmentDate.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentTime.format(TIME_FORMATTER));
            context.setVariable("consultantName", consultantName);
            context.setVariable("appointmentUrl", frontendUrl + "/appointments/" + appointment.getId());

            String html = templateEngine.process("appointment-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("[SHeathCare] Nhắc nhở lịch hẹn ngày mai");
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Đã gửi email nhắc nhở lịch hẹn tới " + customerEmail);

        } catch (Exception e) {
            System.out.println("Lỗi gửi email nhắc nhở lịch hẹn: " + e.getMessage());
        }
    }

    /**
     * Gửi email xác nhận lịch hẹn
     */
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            if (appointment == null || appointment.getCustomer() == null || 
                appointment.getCustomer().getEmail() == null) {
                System.out.println("Không thể gửi email xác nhận: Thiếu thông tin lịch hẹn hoặc email khách hàng");
                return;
            }

            String customerEmail = appointment.getCustomer().getEmail();
            String customerName = appointment.getCustomer().getFullname();
            String serviceName = appointment.getService() != null ? 
                    appointment.getService().getName() : "Chưa xác định";

            LocalDate appointmentDate = appointment.getPreferredDate();
            LocalTime appointmentTime = appointment.getServiceSlotPool() != null ? 
                    appointment.getServiceSlotPool().getStartTime() : LocalTime.of(0, 0);

            String consultantName = appointment.getConsultant() != null ? 
                    appointment.getConsultant().getFullname() : "Chưa xác định";

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("serviceName", serviceName);
            context.setVariable("appointmentDate", appointmentDate.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentTime.format(TIME_FORMATTER));
            context.setVariable("consultantName", consultantName);
            context.setVariable("appointmentUrl", frontendUrl + "/appointments/" + appointment.getId());

            String html = templateEngine.process("appointment-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("[SHeathCare] Xác nhận lịch hẹn");
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Đã gửi email xác nhận lịch hẹn tới " + customerEmail);

        } catch (Exception e) {
            System.out.println("Lỗi gửi email xác nhận lịch hẹn: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo hủy lịch hẹn
     */
    public void sendAppointmentCancellation(Appointment appointment, String reason) {
        try {
            if (appointment == null || appointment.getCustomer() == null || 
                appointment.getCustomer().getEmail() == null) {
                System.out.println("Không thể gửi email hủy lịch: Thiếu thông tin lịch hẹn hoặc email khách hàng");
                return;
            }

            String customerEmail = appointment.getCustomer().getEmail();
            String customerName = appointment.getCustomer().getFullname();
            String serviceName = appointment.getService() != null ? 
                    appointment.getService().getName() : "Chưa xác định";

            LocalDate appointmentDate = appointment.getPreferredDate();
            LocalTime appointmentTime = appointment.getServiceSlotPool() != null ? 
                    appointment.getServiceSlotPool().getStartTime() : LocalTime.of(0, 0);

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("serviceName", serviceName);
            context.setVariable("appointmentDate", appointmentDate.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentTime.format(TIME_FORMATTER));
            context.setVariable("reason", reason);
            context.setVariable("bookingUrl", frontendUrl + "/booking");

            String html = templateEngine.process("appointment-cancellation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("[SHeathCare] Thông báo hủy lịch hẹn");
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Đã gửi email thông báo hủy lịch hẹn tới " + customerEmail);

        } catch (Exception e) {
            System.out.println("Lỗi gửi email hủy lịch hẹn: " + e.getMessage());
        }
    }
}
