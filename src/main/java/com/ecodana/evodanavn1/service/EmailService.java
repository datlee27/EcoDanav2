package com.ecodana.evodanavn1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
// import org.springframework.scheduling.annotation.Async; // Removed this import

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
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


    // Removed @Async annotation to ensure synchronous execution after transaction commit
    public void sendPasswordResetEmail(String to, String token, String baseUrl) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(new InternetAddress(fromEmail, "EcoDana Team"));
        helper.setTo(to);
        helper.setSubject("EcoDana - Yêu cầu đặt lại mật khẩu của bạn");

        // Tạo URL reset từ baseUrl đã được truyền vào
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        String htmlContent = buildPasswordResetHtmlContent(resetUrl);
        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    private String buildPasswordResetHtmlContent(String resetUrl) {
        return "<!DOCTYPE html>"
                + "<html>" // ... (Nội dung HTML cho email reset, xem ví dụ bên dưới)
                + "<body style='font-family: Arial, sans-serif; text-align: center; color: #333;'>"
                + "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px;'>"
                + "<h2>Yêu cầu đặt lại mật khẩu</h2>"
                + "<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản EcoDana của bạn.</p>"
                + "<p>Vui lòng nhấp vào nút bên dưới để đặt lại mật khẩu của bạn. Liên kết này sẽ hết hạn sau 15 phút.</p>"
                + "<a href='" + resetUrl + "' style='background-color: #28a745; color: white; padding: 15px 25px; text-align: center; text-decoration: none; display: inline-block; border-radius: 5px; font-size: 16px; margin: 20px 0;'>Đặt lại mật khẩu</a>"
                + "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                + "<hr><p style='font-size: 12px; color: #888;'>© 2025 EcoDana. All rights reserved.</p>"
                + "</div></body></html>";
    }
}