package com.S_Health.GenderHealthCare.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;


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

}
