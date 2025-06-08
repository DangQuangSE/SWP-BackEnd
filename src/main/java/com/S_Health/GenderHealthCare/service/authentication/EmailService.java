package com.S_Health.GenderHealthCare.service.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác minh đăng ký");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã sẽ hết hạn sau 30 giây.");
        mailSender.send(message);
    }

    public void sendForgotPasswordOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Khôi phục mật khẩu - Mã xác minh");
        message.setText("Mã OTP để đặt lại mật khẩu của bạn là: " + otp + ". Mã này sẽ hết hạn sau 30 giây.");
        mailSender.send(message);
    }

}
