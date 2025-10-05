package com.ecodana.evodanavn1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
// --- THÊM CÁC IMPORT ĐỂ LẤY THỜI GIAN ---
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
// --- KẾT THÚC THÊM IMPORT ---

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otp) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(new InternetAddress(fromEmail, "EcoDana Team"));
        helper.setTo(to);

        // ===== THAY ĐỔI ĐỂ TIÊU ĐỀ EMAIL LÀ DUY NHẤT =====
        // 1. Lấy thời gian hiện tại
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timestamp = LocalTime.now().format(formatter);

        // 2. Tạo tiêu đề email độc nhất bằng cách thêm thời gian vào cuối
        String uniqueSubject = "Your EcoDana Account Verification OTP [" + timestamp + "]";
        helper.setSubject(uniqueSubject);
        // ===============================================

        String htmlContent = buildOtpHtmlContent(otp);
        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
    }

    private String buildOtpHtmlContent(String otp) {
        // Phần code HTML này giữ nguyên, không cần thay đổi
        return "<!DOCTYPE html>"
                + "<html lang='vi'>"
                + "<head><style>"
                + "body {font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;}"
                + ".container {max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 40px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); text-align: center;}"
                + ".header {font-size: 28px; color: #1E7E34; margin-bottom: 20px; font-weight: bold;}"
                + ".otp-code {font-size: 36px; font-weight: bold; color: #ffffff; background-color: #28A745; padding: 15px 25px; border-radius: 5px; letter-spacing: 5px; display: inline-block; margin: 20px 0;}"
                + ".message {font-size: 16px; color: #333333; line-height: 1.5;}"
                + ".footer {font-size: 12px; color: #888888; margin-top: 30px;}"
                + "</style></head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>EcoDana Verification</div>"
                + "<p class='message'>Your One-Time Password (OTP) for account verification is:</p>"
                + "<div class='otp-code'>" + otp + "</div>"
                + "<p class='message'>This code is valid for 5 minutes. Please do not share this code with anyone.</p>"
                + "<p class='footer'>© 2025 EcoDana. All rights reserved.</p>"
                + "</div>"
                + "</body></html>";
    }
}