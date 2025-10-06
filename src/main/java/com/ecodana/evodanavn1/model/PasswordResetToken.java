package com.ecodana.evodanavn1.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PasswordResetTokens")
public class PasswordResetToken {

    @Id
    @Column(name = "Id", length = 36)
    private String id;

    @Column(name = "Token", nullable = false, unique = true)
    private String token;

    @Column(name = "ExpiryTime", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "IsUsed", nullable = false)
    private boolean isUsed = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "UserId")
    private User user;

    // Constructors
    public PasswordResetToken() {
        this.createdAt = LocalDateTime.now();
        this.isUsed = false;
    }

    public PasswordResetToken(String id, String token, User user, LocalDateTime expiryTime) {
        this();
        this.id = id;
        this.token = token;
        this.user = user;
        this.expiryTime = expiryTime;
    }


    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean isUsed) { this.isUsed = isUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}