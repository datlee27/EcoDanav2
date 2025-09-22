package com.ecodana.evodanavn1.service;

import java.util.Optional;

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
        // Try to find user by username first
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        // If not found by username, try to find by email
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public boolean register(User user) {
        try {
            // Check if username or email already exists
            if (userRepository.existsByUsername(user.getUsername())) {
                return false; // Username already exists
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                return false; // Email already exists
            }
            
            // Generate UUID for id if not set
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(java.util.UUID.randomUUID().toString());
            }
            
            // Encode password before saving (only if password is not empty)
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            // Save user to database
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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