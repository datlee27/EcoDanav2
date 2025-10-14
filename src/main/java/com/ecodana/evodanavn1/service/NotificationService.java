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
    public boolean markAsRead(String notificationId) {
        try {
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get unread count
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * Get recent notifications (limit)
     */
    public List<Notification> getRecentNotifications(String userId, int limit) {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
        return all.size() > limit ? all.subList(0, limit) : all;
    }
    
    /**
     * Delete notification
     */
    public boolean deleteNotification(String notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Delete all read notifications for a user
     */
    @Transactional
    public int deleteReadNotifications(String userId) {
        List<Notification> readNotifications = notificationRepository.findByUserIdAndIsReadTrueOrderByCreatedDateDesc(userId);
        int count = readNotifications.size();
        notificationRepository.deleteAll(readNotifications);
        return count;
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
     * Create notification for all staff
     */
    public void createNotificationForAllStaff(String message) {
        List<User> staffList = userRepository.findByRoleName("STAFF");
        for (User staff : staffList) {
            createNotification(staff.getId(), message);
        }
    }
    
    /**
     * Create notification for all staff with related entity
     */
    public void createNotificationForAllStaff(String message, String relatedId, String notificationType) {
        List<User> staffList = userRepository.findByRoleName("STAFF");
        for (User staff : staffList) {
            createNotification(staff.getId(), message, relatedId, notificationType);
        }
    }
    
    /**
     * Create notification for owner of a vehicle
     */
    public void createNotificationForOwner(String ownerId, String message, String relatedId, String notificationType) {
        createNotification(ownerId, message, relatedId, notificationType);
    }
}
