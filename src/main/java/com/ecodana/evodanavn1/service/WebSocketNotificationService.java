package com.ecodana.evodanavn1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo realtime cho Owner khi có booking mới
     */
    public void notifyOwnerNewBooking(String bookingCode, String customerName, Double amount) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "NEW_BOOKING");
        notification.put("bookingCode", bookingCode);
        notification.put("customerName", customerName);
        notification.put("amount", amount);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        notification.put("message", "Có đơn đặt xe mới từ " + customerName);
        
        // Gửi đến topic /topic/owner/notifications
        messagingTemplate.convertAndSend("/topic/owner/notifications", notification);
    }

    /**
     * Gửi thông báo cho Customer khi Owner duyệt/từ chối
     */
    public void notifyCustomerBookingStatus(String userId, String bookingCode, String status, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "BOOKING_STATUS_UPDATE");
        notification.put("bookingCode", bookingCode);
        notification.put("status", status);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // Gửi đến queue riêng của user
        messagingTemplate.convertAndSend("/queue/user/" + userId + "/notifications", notification);
    }

    /**
     * Gửi thông báo cho Staff về việc hoàn tiền
     */
    public void notifyStaffRefund(String bookingCode, String customerName, Double amount, String reason) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "REFUND_REQUEST");
        notification.put("bookingCode", bookingCode);
        notification.put("customerName", customerName);
        notification.put("amount", amount);
        notification.put("reason", reason);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        notification.put("message", "Cần xử lý hoàn tiền cho đơn " + bookingCode);
        
        // Gửi đến topic /topic/staff/notifications
        messagingTemplate.convertAndSend("/topic/staff/notifications", notification);
    }

    /**
     * Gửi thông báo broadcast cho tất cả users
     */
    public void broadcastNotification(String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
