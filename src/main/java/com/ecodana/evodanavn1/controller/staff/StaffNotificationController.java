package com.ecodana.evodanavn1.controller.staff;

import com.ecodana.evodanavn1.model.Notification;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff/notifications")
public class StaffNotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Hiển thị trang thông báo
     */
    @GetMapping
    public String showNotifications(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy tất cả thông báo của staff
        List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
        
        // Đếm số thông báo chưa đọc
        long unreadCount = notifications.stream()
                .filter(n -> !n.getIsRead())
                .count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "staff/notifications";
    }

    /**
     * API: Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        long unreadCount = notificationService.getUnreadCount(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API: Lấy danh sách thông báo mới nhất
     */
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getRecentNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<Notification> notifications = notificationService.getRecentNotifications(currentUser.getId(), 10);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đánh dấu thông báo đã đọc
     */
    @PostMapping("/{notificationId}/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = notificationService.markAsRead(notificationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.markAllAsRead(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã đánh dấu tất cả thông báo là đã đọc");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa thông báo
     */
    @DeleteMapping("/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable String notificationId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = notificationService.deleteNotification(notificationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa tất cả thông báo đã đọc
     */
    @DeleteMapping("/delete-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReadNotifications(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        int deletedCount = notificationService.deleteReadNotifications(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deletedCount", deletedCount);
        response.put("message", "Đã xóa " + deletedCount + " thông báo");
        
        return ResponseEntity.ok(response);
    }
}
