package com.ecodana.evodanavn1.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.RoleService;
import com.ecodana.evodanavn1.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2User oauth2User) {
                
                // Get user info from Google
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String sub = oauth2User.getAttribute("sub");
                
                System.out.println("OAuth2 Login - Email: " + email + ", Name: " + name);
                System.out.println("OAuth2 Login - All attributes: " + oauth2User.getAttributes());
                
                if (email == null || email.isEmpty()) {
                    System.err.println("OAuth2 Login failed: No email provided");
                    response.sendRedirect("/login?error=no_email");
                    return;
                }

                // Check if user already exists by email
                User existingUser = userService.findByEmail(email);
                User user;
                
                if (existingUser != null) {
                    // User exists, update with latest info from Google
                    user = existingUser;
                    
                    // Update user info with latest from Google
                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts[0]);
                        if (nameParts.length > 1) {
                            user.setLastName(nameParts[1]);
                        }
                    }
                    
                    // Update username to use real name
                    String username = name != null && !name.isEmpty() ? name.replace(" ", "_") : email.split("@")[0];
                    user.setUsername(username);
                    
                    // Save updated user
                    userService.register(user);
                    
                    System.out.println("OAuth2 Login - Existing user updated: " + user.getEmail());
                } else {
                    // Create new user
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    // Use real name as username if available, otherwise use email prefix
                    String username = name != null && !name.isEmpty() ? name.replace(" ", "_") : email.split("@")[0];
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword("OAUTH_USER_" + UUID.randomUUID().toString()); // Generate a random password for OAuth users
                    user.setPhoneNumber("");
                    // Role will be set via roleId, not the role field
                    user.setHasLicense(false);
                    user.setActive(true);
                    
                    // Set additional fields for database compatibility
                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts[0]);
                        if (nameParts.length > 1) {
                            user.setLastName(nameParts[1]);
                        }
                    }
                    
                    // Set required database fields
                    String customerRoleId = roleService.getDefaultCustomerRoleId();
                    System.out.println("OAuth2 Login - Setting customer role ID: " + customerRoleId);
                    user.setRoleId(customerRoleId);
                    user.setNormalizedUserName(user.getUsername().toUpperCase());
                    user.setNormalizedEmail(user.getEmail().toUpperCase());
                    user.setSecurityStamp(UUID.randomUUID().toString());
                    user.setConcurrencyStamp(UUID.randomUUID().toString());
                    user.setStatus("Active");
                    user.setEmailVerifed(true);
                    user.setTwoFactorEnabled(false);
                    user.setLockoutEnabled(false);
                    user.setAccessFailedCount(0);
                    user.setCreatedDate(java.time.LocalDateTime.now());
                    
                    // Save user to database
                    boolean registered = userService.register(user);
                    if (!registered) {
                        System.err.println("OAuth2 Login failed: Could not register new user");
                        response.sendRedirect("/login?error=registration_failed");
                        return;
                    }
                    System.out.println("OAuth2 Login - New user created: " + user.getEmail());
                    System.out.println("OAuth2 Login - User role ID: " + user.getRoleId());
                }

                // Check if user is active
                if (!user.isActive()) {
                    System.err.println("OAuth2 Login failed: User account is inactive");
                    response.sendRedirect("/login?error=account_inactive");
                    return;
                }

                // Reload user with full role information
                User fullUser = userService.findByEmailWithRole(user.getEmail());
                if (fullUser != null) {
                    user = fullUser;
                    System.out.println("OAuth2 Login - User reloaded with role: " + user.getRoleName());
                } else {
                    System.out.println("OAuth2 Login - Could not reload user with role, using original user");
                }
                
                // Set user in session
                HttpSession session = request.getSession(true);
                session.setAttribute("currentUser", user);
                System.out.println("OAuth2 Login successful: " + user.getEmail());
                System.out.println("User role: " + user.getRoleName());
                System.out.println("User role object: " + (user.getRole() != null ? user.getRole().getRoleName() : "null"));
                System.out.println("Session ID: " + session.getId());
                System.out.println("User set in session: " + (session.getAttribute("currentUser") != null));
                
                // Redirect based on role
                String roleName = user.getRoleName();
                if ("Admin".equalsIgnoreCase(roleName)) {
                    session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Admin " + user.getFirstName() + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                    response.sendRedirect("/admin");
                } else if ("Owner".equalsIgnoreCase(roleName)) {
                    session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Owner " + user.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                    response.sendRedirect("/owner/dashboard");
                } else if ("Staff".equalsIgnoreCase(roleName)) {
                    session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Staff " + user.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                    response.sendRedirect("/staff");
                } else if ("Customer".equalsIgnoreCase(roleName)) {
                    session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng " + user.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                    response.sendRedirect("/");
                } else {
                    session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng " + user.getFirstName() + " trở lại EvoDana.");
                    response.sendRedirect("/");
                }
            } else {
                System.err.println("OAuth2 Login failed: Invalid principal type");
                response.sendRedirect("/login?error=invalid_principal");
                return;
            }
        } catch (IOException e) {
            System.err.println("OAuth2 Login error: " + e.getMessage());
            response.sendRedirect("/login?error=oauth_error");
            return;
        }
    }

}
