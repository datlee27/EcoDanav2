package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    // Mock login method
    public User login(String username, String password, String secretKey) {
        if ("demo".equals(username) && "demo".equals(password)) {
            return new User("1", "demo", "demo@example.com", "customer", true);
        } else if ("staff".equals(username) && "staff123".equals(password)) {
            return new User("2", "staff", "staff@example.com", "staff", true);
        } else if ("admin".equals(username) && "admin123".equals(password) && "secretadmin".equals(secretKey)) {
            return new User("3", "admin", "admin@example.com", "admin", true);
        }
        return null;
    }

    // Mock register (chỉ alert trong demo, sau này lưu DB)
    public boolean register(User user) {
        // Simulate success
        return true;
    }
}