package com.ecodana.evodanavn1.service;

import java.util.List;
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
    
    @Autowired
    private com.ecodana.evodanavn1.service.RoleService roleService;

    public User login(String username, String password, String secretKey) {
        // Try to find user by username first
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        // If not found by username, try to find by email
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            if (passwordMatches) {
                // Access role to trigger JPA loading
                user.getRole();
                return user;
            }
        }
        return null;
    }

    public boolean register(User user) {
        try {
            // Validate input
            if (user == null) {
                System.err.println("User object is null");
                return false;
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                System.err.println("Email is required");
                return false;
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                System.err.println("Password is required");
                return false;
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                System.err.println("Email already exists: " + user.getEmail());
                return false;
            }
            
            // Check if username already exists (if provided)
            if (user.getUsername() != null && !user.getUsername().isEmpty() && 
                userRepository.existsByUsername(user.getUsername())) {
                System.err.println("Username already exists: " + user.getUsername());
                return false;
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
                user.setRoleId(roleService.getDefaultCustomerRoleId());
                System.out.println("Set default role ID: " + user.getRoleId());
            } else if (!roleService.isValidRoleId(user.getRoleId())) {
                System.err.println("Invalid role ID: " + user.getRoleId() + ", using default customer role");
                user.setRoleId(roleService.getDefaultCustomerRoleId());
                System.out.println("Set default role ID due to invalid role: " + user.getRoleId());
            }
            
            // Set additional required fields
            user.setActive(true); // This will also set status = "Active"
            user.setEmailVerifed(false);
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
            System.out.println("User registered successfully: " + user.getEmail());
            return true;
        } catch (Exception e) {
            System.err.println("Error in UserService.register(): " + e.getMessage());
            return false;
        }
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public User findByIdWithRole(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Trigger role loading by accessing the role field
            user.getRole();
            return user;
        }
        return null;
    }
    
    public User findByUsernameWithRole(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Trigger role loading by accessing the role field
            user.getRole();
            return user;
        }
        return null;
    }
    
    public User findByEmailWithRole(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Trigger role loading by accessing the role field
            user.getRole();
            return user;
        }
        return null;
    }
    
    /**
     * Get user with role information for authentication
     * @param usernameOrEmail username or email
     * @return User with role loaded, or null if not found
     */
    public User getUserWithRole(String usernameOrEmail) {
        // Try username first
        User user = findByUsernameWithRole(usernameOrEmail);
        if (user != null) {
            return user;
        }
        
        // Try email if username not found
        user = findByEmailWithRole(usernameOrEmail);
        return user;
    }
    
    /**
     * Check if user has specific role
     * @param user the user to check
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(User user, String roleName) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return roleName.equalsIgnoreCase(user.getRole().getRoleName());
    }
    
    /**
     * Check if user is admin
     * @param user the user to check
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin(User user) {
        return hasRole(user, "Admin");
    }
    
    /**
     * Check if user is staff
     * @param user the user to check
     * @return true if user is staff, false otherwise
     */
    public boolean isStaff(User user) {
        return hasRole(user, "Staff");
    }
    
    /**
     * Check if user is owner
     * @param user the user to check
     * @return true if user is owner, false otherwise
     */
    public boolean isOwner(User user) {
        return hasRole(user, "Owner");
    }
    
    /**
     * Check if user is customer
     * @param user the user to check
     * @return true if user is customer, false otherwise
     */
    public boolean isCustomer(User user) {
        return hasRole(user, "Customer");
    }
    
    /**
     * Get all users from database
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Update user in database
     * @param user the user to update
     * @return updated user
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}