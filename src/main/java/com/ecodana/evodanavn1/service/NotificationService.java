package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Notification;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.NotificationRepository;
import com.ecodana.evodanavn1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create notification for a specific user
     */
    public Notification createNotification(String userId, String message) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }
    
    /**
     * Create notification with related entity
     */
    public Notification createNotification(String userId, String message, String relatedId, String notificationType) {
        Notification notification = new Notification();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setNotificationType(notificationType);
        return notificationRepository.save(notification);
    }
    
    /**
     * Create notification for all admins
     */
    public void createNotificationForAllAdmins(String message) {
        List<User> admins = userRepository.findByRoleName("ADMIN");
        for (User admin : admins) {
            createNotification(admin.getId(), message);
        }
    }
    
    /**
     * Create notification for all admins with related entity
     */
    public void createNotificationForAllAdmins(String message, String relatedId, String notificationType) {
        List<User> admins = userRepository.findByRoleName("ADMIN");
        for (User admin : admins) {
            createNotification(admin.getId(), message, relatedId, notificationType);
        }
    }
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedDateDesc(userId);
    }
    
    /**
     * Count unread notifications
     */
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Gửi thông báo khi có booking mới cho Owner
     */
    public void notifyOwnerNewBooking(com.ecodana.evodanavn1.model.Booking booking) {
        String ownerId = booking.getVehicle().getOwnerId();
        if (ownerId != null) {
            String message = String.format(
                "Bạn có yêu cầu đặt xe mới #%s. Vui lòng phản hồi trong vòng 2 giờ.",
                booking.getBookingCode()
            );
            createNotification(ownerId, message, booking.getBookingId(), "BOOKING_REQUEST");
        }
    }
    
    /**
     * Gửi thông báo khi Owner chấp nhận booking
     */
    public void notifyCustomerBookingApproved(com.ecodana.evodanavn1.model.Booking booking) {
        String customerId = booking.getUser().getId();
        String message = String.format(
            "Yêu cầu đặt xe #%s đã được chấp nhận. Vui lòng thanh toán để xác nhận đơn hàng.",
            booking.getBookingCode()
        );
        createNotification(customerId, message, booking.getBookingId(), "BOOKING_APPROVED");
    }
    
    /**
     * Gửi thông báo khi Owner từ chối booking
     */
    public void notifyCustomerBookingRejected(com.ecodana.evodanavn1.model.Booking booking, String reason) {
        String customerId = booking.getUser().getId();
        String message = String.format(
            "Yêu cầu đặt xe #%s đã bị từ chối. Lý do: %s",
            booking.getBookingCode(),
            reason != null ? reason : "Không có lý do cụ thể"
        );
        createNotification(customerId, message, booking.getBookingId(), "BOOKING_REJECTED");
    }
    
    /**
     * Gửi thông báo khi thanh toán thành công
     */
    public void notifyPaymentSuccess(com.ecodana.evodanavn1.model.Booking booking, 
                                     com.ecodana.evodanavn1.model.Payment payment) {
        // Thông báo cho Customer
        String customerMessage = String.format(
            "Thanh toán thành công %s VNĐ cho đơn hàng #%s. Cảm ơn bạn đã sử dụng dịch vụ!",
            payment.getAmount(),
            booking.getBookingCode()
        );
        createNotification(booking.getUser().getId(), customerMessage, payment.getPaymentId(), "PAYMENT_SUCCESS");
        
        // Thông báo cho Admin
        String adminMessage = String.format(
            "Đơn hàng #%s đã được thanh toán thành công. Số tiền: %s VNĐ. Khách hàng: %s",
            booking.getBookingCode(),
            payment.getAmount(),
            booking.getUser().getUsername()
        );
        createNotificationForAllAdmins(adminMessage, payment.getPaymentId(), "PAYMENT_SUCCESS");
        
        // Thông báo cho Owner
        String ownerId = booking.getVehicle().getOwnerId();
        if (ownerId != null) {
            String ownerMessage = String.format(
                "Đơn đặt xe #%s đã được thanh toán. Vui lòng chuẩn bị xe cho khách hàng.",
                booking.getBookingCode()
            );
            createNotification(ownerId, ownerMessage, booking.getBookingId(), "BOOKING_PAID");
        }
    }
    
    /**
     * Gửi thông báo khi booking bị tự động reject do Owner không phản hồi
     */
    public void notifyBookingAutoRejected(com.ecodana.evodanavn1.model.Booking booking) {
        String customerId = booking.getUser().getId();
        String message = String.format(
            "Yêu cầu đặt xe #%s đã bị hủy do chủ xe không phản hồi trong thời gian quy định.",
            booking.getBookingCode()
        );
        createNotification(customerId, message, booking.getBookingId(), "BOOKING_AUTO_REJECTED");
    }

    /**
     * Gửi thông báo cho khách hàng khi rental bắt đầu (Owner giao xe)
     */
    public void notifyCustomerRentalStarted(com.ecodana.evodanavn1.model.Booking booking) {
        String customerId = booking.getUser().getId();
        String message = String.format(
                "Chuyến đi #%s của bạn đã bắt đầu. Chúc bạn lái xe an toàn!",
                booking.getBookingCode()
        );
        createNotification(customerId, message, booking.getBookingId(), "RENTAL_STARTED");
    }
}
