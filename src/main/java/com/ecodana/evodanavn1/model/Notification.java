package com.ecodana.evodanavn1.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Notification")
public class Notification {
    @Id
    @Column(name = "NotificationId", length = 36)
    private String notificationId;
    
    @Column(name = "UserId", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "Message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;
    
    // Constructors
    public Notification() {
        this.createdDate = LocalDateTime.now();
        this.isRead = false;
    }
    
    // Getters/Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
}
