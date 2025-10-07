package com.ecodana.evodanavn1.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ecodana.evodanavn1.model.PasswordResetToken;
import com.ecodana.evodanavn1.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Role;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.UserRepository;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private com.ecodana.evodanavn1.service.RoleService roleService;

    public User login(String username, String password, String secretKey) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(username);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            if (passwordMatches) {
                user.getRole();
                return user;
            }
        }
        return null;
    }

    public boolean register(User user) {
        try {
            if (user == null) {
                return false;
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return false;
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return false;
            }
            
            if (userRepository.existsByEmail(user.getEmail())) {
                return false;
            }
            
            if (user.getUsername() != null && !user.getUsername().isEmpty() && 
                userRepository.existsByUsername(user.getUsername())) {
                return false;
            }
            
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString());
            }
            
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                String email = user.getEmail();
                String username = email.split("@")[0] + "_" + System.currentTimeMillis();
                user.setUsername(username);
            }
            
            user.setNormalizedUserName(user.getUsername().toUpperCase());
            user.setNormalizedEmail(user.getEmail().toUpperCase());
            user.setSecurityStamp(UUID.randomUUID().toString());
            user.setConcurrencyStamp(UUID.randomUUID().toString());
            
            if (user.getFirstName() == null) user.setFirstName("");
            if (user.getLastName() == null) user.setLastName("");
            
            if (user.getRoleId() == null || user.getRoleId().isEmpty() || !roleService.isValidRoleId(user.getRoleId())) {
                user.setRoleId(roleService.getDefaultCustomerRoleId());
            }
            
            user.setActive(true);
            user.setTwoFactorEnabled(false);
            user.setLockoutEnabled(false);
            user.setAccessFailedCount(0);
            user.setCreatedDate(java.time.LocalDateTime.now());
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            logger.error("Error in UserService.register(): " + e.getMessage(), e);
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
        return userRepository.findById(id).map(user -> {
            user.getRole();
            return user;
        }).orElse(null);
    }
    
    public User findByUsernameWithRole(String username) {
        return userRepository.findByUsername(username).map(user -> {
            user.getRole();
            return user;
        }).orElse(null);
    }
    
    public User findByEmailWithRole(String email) {
        return userRepository.findByEmail(email).map(user -> {
            user.getRole();
            return user;
        }).orElse(null);
    }
    
    public User getUserWithRole(String usernameOrEmail) {
        User user = findByUsernameWithRole(usernameOrEmail);
        if (user != null) {
            return user;
        }
        return findByEmailWithRole(usernameOrEmail);
    }
    
    public boolean hasRole(User user, String roleName) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return roleName.equalsIgnoreCase(user.getRole().getRoleName());
    }
    
    public boolean isAdmin(User user) {
        return hasRole(user, "Admin");
    }
    
    public boolean isStaff(User user) {
        return hasRole(user, "Staff");
    }
    
    public boolean isOwner(User user) {
        return hasRole(user, "Owner");
    }

    public boolean isCustomer(User user) {
        return hasRole(user, "Customer");
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getAllUsersWithRole() {
        try {
            return userRepository.findAllWithRoles();
        } catch (Exception e) {
            logger.error("Error loading users with roles: " + e.getMessage(), e);
            return userRepository.findAll();
        }
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(String id, String firstName, String lastName, LocalDate userDOB, String gender, String phoneNumber) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUserDOB(userDOB);
            user.setGender(gender);
            user.setPhoneNumber(phoneNumber);
            return userRepository.save(user);
        }
        return null;
    }
    
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<User> allUsers = getAllUsers();
        long activeUsers = allUsers.stream().filter(User::isActive).count();
        long pendingUsers = allUsers.stream().filter(u -> "Pending".equals(u.getStatus())).count();
        long suspendedUsers = allUsers.stream().filter(u -> "Suspended".equals(u.getStatus())).count();
        
        stats.put("totalUsers", allUsers.size());
        stats.put("activeUsers", activeUsers);
        stats.put("pendingUsers", pendingUsers);
        stats.put("suspendedUsers", suspendedUsers);
        
        return stats;
    }
    
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }
    
    public List<User> getRecentUsers(int limit) {
        return userRepository.findRecentUsers().stream().limit(limit).toList();
    }
    
    public boolean suspendUser(String userId) {
        return userRepository.findById(userId).map(user -> {
            user.setStatus("Suspended");
            user.setActive(false);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
    
    public boolean activateUser(String userId) {
        return userRepository.findById(userId).map(user -> {
            user.setStatus("Active");
            user.setActive(true);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
    
    public boolean deleteUser(String userId) {
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting user: " + e.getMessage(), e);
            return false;
        }
    }
    
    public boolean updateUserRole(String userId, String roleId) {
        if (!roleService.isValidRoleId(roleId)) {
            return false;
        }
        
        return userRepository.findById(userId).map(user -> {
            user.setRoleId(roleId);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }


    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public PasswordResetToken createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        // Token hết hạn sau 15 phút
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);
        PasswordResetToken myToken = new PasswordResetToken(id, token, user, expiryDate);
        return tokenRepository.save(myToken);
    }

    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        return Optional.ofNullable(tokenRepository.findByToken(token));
    }

    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> passTokenOpt = getPasswordResetToken(token);
        if (passTokenOpt.isEmpty()) {
            return "invalidToken";
        }

        PasswordResetToken passToken = passTokenOpt.get();
        if (passToken.isUsed()) {
            return "usedToken";
        }

        if (passToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "expired";
        }

        return null; // Token hợp lệ
    }

    /**
     * Thay đổi mật khẩu cho người dùng đã xác thực.
     *
     * @param userId          ID của người dùng cần đổi mật khẩu.
     * @param currentPassword Mật khẩu hiện tại để xác thực.
     * @param newPassword     Mật khẩu mới.
     * @return true nếu đổi mật khẩu thành công, false nếu mật khẩu hiện tại không đúng.
     */
    @Transactional
    public boolean changePasswordForAuthenticatedUser(String userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Kiểm tra xem mật khẩu hiện tại có khớp không
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                // Nếu khớp, mã hóa và đặt mật khẩu mới
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        // Trả về false nếu không tìm thấy người dùng hoặc mật khẩu hiện tại không đúng
        return false;
    }

    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }
    
    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
    
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}