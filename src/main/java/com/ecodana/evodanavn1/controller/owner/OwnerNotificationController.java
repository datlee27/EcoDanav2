package com.ecodana.evodanavn1.controller.owner;

import com.ecodana.evodanavn1.model.Notification;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/notifications")
public class OwnerNotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String showNotifications(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        List<Notification> notifications = notificationService.getNotificationsByUserId(owner.getId());
        long unreadCount = notificationService.getUnreadCount(owner.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentUser", owner);

        return "owner/notifications";
    }

    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public String markAsRead(@PathVariable String id) {
        boolean success = notificationService.markAsRead(id);
        return success ? "success" : "error";
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public String markAllAsRead(HttpSession session) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) return "error";
        
        notificationService.markAllAsRead(owner.getId());
        return "success";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteNotification(@PathVariable String id) {
        boolean success = notificationService.deleteNotification(id);
        return success ? "success" : "error";
    }

    @DeleteMapping("/delete-read")
    @ResponseBody
    public String deleteReadNotifications(HttpSession session) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) return "error";
        
        int count = notificationService.deleteReadNotifications(owner.getId());
        return String.valueOf(count);
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public long getUnreadCount(HttpSession session) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) return 0;
        
        return notificationService.getUnreadCount(owner.getId());
    }
}
