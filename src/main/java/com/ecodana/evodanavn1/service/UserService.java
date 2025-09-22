package com.ecodana.evodanavn1.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User login(String username, String password, String secretKey) {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Username/Email: " + username);
        System.out.println("Password: " + password);
        
        // Try to find user by username first
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        System.out.println("Found by username: " + userOpt.isPresent());
        
        // If not found by username, try to find by email
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(username.trim());
            System.out.println("Found by email: " + userOpt.isPresent());
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername());
            System.out.println("User email: " + user.getEmail());
            System.out.println("User status: " + user.getStatus());
            System.out.println("User active: " + user.isActive());
            System.out.println("User roleId: " + user.getRoleId());
            System.out.println("Stored password hash: " + user.getPassword());
            
            // Check if user is active first
            if (!user.isActive()) {
                System.out.println("User is not active!");
                return null;
            }
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (passwordMatches) {
                System.out.println("Login successful!");
                // Load role if not already loaded
                if (user.getRole() == null && user.getRoleId() != null) {
                    // Create a simple role object for template compatibility
                    com.ecodana.evodanavn1.model.Role role = new com.ecodana.evodanavn1.model.Role();
                    role.setRoleId(user.getRoleId());
                    // Set role name based on roleId
                    switch (user.getRoleId()) {
                        case "customer-role-id" -> role.setRoleName("Customer");
                        case "staff-role-id" -> role.setRoleName("Staff");
                        case "admin-role-id" -> role.setRoleName("Admin");
                        default -> role.setRoleName("Customer"); // Default
                    }
                    user.setRole(role);
                }
                return user;
            } else {
                System.out.println("Password does not match!");
            }
        } else {
            System.out.println("User not found!");
        }
        System.out.println("=== END LOGIN DEBUG ===");
        return null;
    }

    public boolean register(User user) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return false; // Email already exists
            }
            
            // Generate UUID for id if not set
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString());
            }
            
            // Generate username if not set
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                String email = user.getEmail();
                String username = email.split("@")[0] + "_" + System.currentTimeMillis();
                user.setUsername(username);
            }
            
            // Set additional required fields for database compatibility
            user.setNormalizedUserName(user.getUsername().toUpperCase());
            user.setNormalizedEmail(user.getEmail().toUpperCase());
            user.setSecurityStamp(UUID.randomUUID().toString());
            user.setConcurrencyStamp(UUID.randomUUID().toString());
            
            // Set default role to CUSTOMER if not set
            if (user.getRoleId() == null || user.getRoleId().isEmpty()) {
                user.setRoleId("customer-role-id"); // Default role ID for customers
            }
            
            // Create role object for template compatibility
            com.ecodana.evodanavn1.model.Role role = new com.ecodana.evodanavn1.model.Role();
            role.setRoleId(user.getRoleId());
            switch (user.getRoleId()) {
                case "customer-role-id" -> role.setRoleName("Customer");
                case "staff-role-id" -> role.setRoleName("Staff");
                case "admin-role-id" -> role.setRoleName("Admin");
                default -> role.setRoleName("Customer"); // Default
            }
            user.setRole(role);
            
            // Set additional required fields
            user.setActive(true); // This will also set status = "Active"
            user.setEmailVerified(false);
            user.setTwoFactorEnabled(false);
            user.setLockoutEnabled(false);
            user.setAccessFailedCount(0);
            user.setCreatedDate(java.time.LocalDateTime.now());
            
            // Encode password before saving (only if password is not empty)
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            // Save user to database
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            System.err.println("Error in UserService.register(): " + e.getMessage());
            System.err.println("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return false;
        }
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}