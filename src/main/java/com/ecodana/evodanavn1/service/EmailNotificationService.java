package com.ecodana.evodanavn1.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email thông báo Owner có booking mới
     */
    public void sendNewBookingNotificationToOwner(String ownerEmail, String bookingCode, String customerName, Double amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(ownerEmail);
            helper.setSubject("[EcoDana] Có đơn đặt xe mới chờ duyệt - " + bookingCode);
            helper.setText(buildNewBookingEmailContent(bookingCode, customerName, amount), true);

            mailSender.send(message);
            System.out.println("✅ Email sent to owner: " + ownerEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to owner: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo Customer khi booking được duyệt
     */
    public void sendBookingApprovedEmail(String customerEmail, String bookingCode, String customerName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("[EcoDana] Đơn đặt xe " + bookingCode + " đã được duyệt");
            helper.setText(buildApprovedEmailContent(bookingCode, customerName), true);

            mailSender.send(message);
            System.out.println("✅ Approval email sent to: " + customerEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send approval email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo Customer khi booking bị từ chối
     */
    public void sendBookingRejectedEmail(String customerEmail, String bookingCode, String customerName, String reason, Double refundAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("[EcoDana] Đơn đặt xe " + bookingCode + " đã bị từ chối");
            helper.setText(buildRejectedEmailContent(bookingCode, customerName, reason, refundAmount), true);

            mailSender.send(message);
            System.out.println("✅ Rejection email sent to: " + customerEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send rejection email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi email thông báo Staff về việc hoàn tiền
     */
    public void sendRefundNotificationToStaff(String staffEmail, String bookingCode, String customerName, Double amount, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(staffEmail);
            helper.setSubject("[EcoDana] Cần xử lý hoàn tiền - " + bookingCode);
            helper.setText(buildRefundStaffEmailContent(bookingCode, customerName, amount, reason), true);

            mailSender.send(message);
            System.out.println("✅ Refund notification sent to staff: " + staffEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send refund notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== Email Templates ====================

    private String buildNewBookingEmailContent(String bookingCode, String customerName, Double amount) {
        return "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".booking-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #667eea; }" +
                ".button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 10px 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🔔 Có đơn đặt xe mới!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào,</p>" +
                "<p>Bạn có một đơn đặt xe mới đã thanh toán và đang chờ duyệt:</p>" +
                "<div class='booking-info'>" +
                "<p><strong>Mã đặt xe:</strong> " + bookingCode + "</p>" +
                "<p><strong>Khách hàng:</strong> " + customerName + "</p>" +
                "<p><strong>Số tiền:</strong> " + String.format("%,.0f", amount) + " ₫</p>" +
                "</div>" +
                "<p>Vui lòng truy cập hệ thống để xem chi tiết và duyệt đơn:</p>" +
                "<div style='text-align: center;'>" +
                "<a href='http://localhost:8080/owner/bookings/pending-approval' class='button'>Xem đơn đặt xe</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động từ hệ thống EcoDana</p>" +
                "<p>© 2025 EcoDana. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildApprovedEmailContent(String bookingCode, String customerName) {
        return "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".success-box { background: #d4edda; border: 1px solid #c3e6cb; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".button { display: inline-block; padding: 12px 30px; background: #11998e; color: white; text-decoration: none; border-radius: 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>✅ Đơn đặt xe đã được duyệt!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<div class='success-box'>" +
                "<p><strong>Tin vui!</strong> Đơn đặt xe <strong>" + bookingCode + "</strong> của bạn đã được chấp nhận.</p>" +
                "</div>" +
                "<p><strong>Các bước tiếp theo:</strong></p>" +
                "<ul>" +
                "<li>Vui lòng đến nhận xe đúng thời gian đã đặt</li>" +
                "<li>Mang theo CMND/CCCD và Giấy phép lái xe</li>" +
                "<li>Kiểm tra tình trạng xe trước khi nhận</li>" +
                "</ul>" +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<a href='http://localhost:8080/booking/my-bookings' class='button'>Xem chi tiết đơn đặt xe</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Cảm ơn bạn đã sử dụng dịch vụ EcoDana!</p>" +
                "<p>© 2025 EcoDana. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildRejectedEmailContent(String bookingCode, String customerName, String reason, Double refundAmount) {
        return "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".warning-box { background: #fff3cd; border: 1px solid #ffc107; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                ".refund-box { background: #d1ecf1; border: 1px solid #bee5eb; padding: 15px; border-radius: 8px; margin: 15px 0; }" +
                ".button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>❌ Thông báo từ chối đơn đặt xe</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<div class='warning-box'>" +
                "<p>Rất tiếc, đơn đặt xe <strong>" + bookingCode + "</strong> của bạn đã bị từ chối.</p>" +
                "<p><strong>Lý do:</strong> " + reason + "</p>" +
                "</div>" +
                "<div class='refund-box'>" +
                "<p><strong>💰 Thông tin hoàn tiền:</strong></p>" +
                "<p>Số tiền <strong>" + String.format("%,.0f", refundAmount) + " ₫</strong> đã được hoàn lại vào tài khoản của bạn.</p>" +
                "<p>Thời gian xử lý: 1-3 ngày làm việc</p>" +
                "</div>" +
                "<p>Chúng tôi rất xin lỗi vì sự bất tiện này. Bạn có thể:</p>" +
                "<ul>" +
                "<li>Đặt xe khác phù hợp hơn</li>" +
                "<li>Liên hệ với chúng tôi để được tư vấn</li>" +
                "</ul>" +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<a href='http://localhost:8080/vehicles' class='button'>Xem các xe khác</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Cảm ơn bạn đã tin tưởng EcoDana!</p>" +
                "<p>Hotline: 0236 123 456 | Email: support@ecodana.com</p>" +
                "<p>© 2025 EcoDana. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    private String buildRefundStaffEmailContent(String bookingCode, String customerName, Double amount, String reason) {
        return "<html>" +
                "<head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #343a40; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".info-box { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }" +
                ".button { display: inline-block; padding: 12px 30px; background: #343a40; color: white; text-decoration: none; border-radius: 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>💳 Thông báo hoàn tiền</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào Staff,</p>" +
                "<p>Có một đơn đặt xe bị từ chối và cần xử lý hoàn tiền:</p>" +
                "<div class='info-box'>" +
                "<p><strong>Mã đặt xe:</strong> " + bookingCode + "</p>" +
                "<p><strong>Khách hàng:</strong> " + customerName + "</p>" +
                "<p><strong>Số tiền hoàn:</strong> " + String.format("%,.0f", amount) + " ₫</p>" +
                "<p><strong>Lý do từ chối:</strong> " + reason + "</p>" +
                "</div>" +
                "<p><strong>⚠️ Lưu ý:</strong> Hệ thống đã tự động tạo payment record với type=Refund. Vui lòng kiểm tra và xử lý hoàn tiền nếu cần.</p>" +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<a href='http://localhost:8080/staff/payments' class='button'>Xem chi tiết thanh toán</a>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Email tự động từ hệ thống EcoDana</p>" +
                "<p>© 2025 EcoDana. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }
}
