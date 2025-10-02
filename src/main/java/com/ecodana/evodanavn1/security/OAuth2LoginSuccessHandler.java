package com.ecodana.evodanavn1.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.security.CustomOAuth2UserService.CustomOidcUser;
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
            User user;
            
            if (principal instanceof CustomOidcUser customOidcUser) {
                user = customOidcUser.getUser();
                System.out.println("OAuth2 Login - Using existing user from CustomOidcUser: " + user.getEmail());
            } else if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                
                System.out.println("OAuth2 Login - Email: " + email + ", Name: " + name);
                
                if (email == null || email.isEmpty()) {
                    System.err.println("OAuth2 Login failed: No email provided");
                    response.sendRedirect("/login?error=no_email");
                    return;
                }

                User existingUser = userService.findByEmail(email);
                
                if (existingUser != null) {
                    user = existingUser;
                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts[0]);
                        if (nameParts.length > 1) {
                            user.setLastName(nameParts[1]);
                        }
                    }
                    user.setUsername(user.getFirstName() + user.getLastName());
                    user.setEmailVerifed(true);
                    userService.register(user);
                    System.out.println("OAuth2 Login - Existing user updated: " + user.getEmail());
                } else {
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setEmail(email);
                    user.setPassword("OAUTH_USER_" + UUID.randomUUID().toString());
                    user.setPhoneNumber("");
                    user.setActive(true);

                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts[0]);
                        if (nameParts.length > 1) {
                            user.setLastName(nameParts[1]);
                        }
                    }
                    user.setUsername(user.getFirstName() + user.getLastName());

                    String assignedRoleId = getAssignedRoleForEmail(email);
                    if (assignedRoleId != null) {
                        user.setRoleId(assignedRoleId);
                    } else {
                        user.setRoleId(roleService.getDefaultCustomerRoleId());
                    }
                    
                    user.setNormalizedUserName(user.getUsername().toUpperCase());
                    user.setNormalizedEmail(user.getEmail().toUpperCase());
                    user.setSecurityStamp(UUID.randomUUID().toString());
                    user.setConcurrencyStamp(UUID.randomUUID().toString());
                    user.setStatus("Active");
                    user.setEmailVerifed(true);
                    user.setCreatedDate(java.time.LocalDateTime.now());
                    
                    boolean registered = userService.register(user);
                    if (!registered) {
                        System.err.println("OAuth2 Login failed: Could not register new user");
                        response.sendRedirect("/login?error=registration_failed");
                        return;
                    }
                    System.out.println("OAuth2 Login - New user created: " + user.getEmail());
                }
            } else {
                System.err.println("OAuth2 Login failed: Invalid principal type");
                response.sendRedirect("/login?error=invalid_principal");
                return;
            }

            if (!user.isActive()) {
                System.err.println("OAuth2 Login failed: User account is inactive");
                response.sendRedirect("/login?error=account_inactive");
                return;
            }

            User fullUser = userService.findByEmailWithRole(user.getEmail());
            if (fullUser != null) {
                user = fullUser;
            }
            
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);
            
            String roleName = user.getRoleName();
            String displayUserName = user.getFirstName() + " " + user.getLastName();

            if ("Admin".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Admin " + displayUserName + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                response.sendRedirect("/admin");
            } else if ("Owner".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Owner " + displayUserName + "! Bạn có thể quản lý xe và đặt chỗ.");
                response.sendRedirect("/owner/dashboard");
            } else if ("Staff".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng Staff " + displayUserName + "! Bạn có thể quản lý xe và đặt chỗ.");
                response.sendRedirect("/staff");
            } else {
                session.setAttribute("flash_success", "🎉 Đăng nhập bằng Google thành công! Chào mừng " + displayUserName + "! Hãy khám phá và đặt xe ngay.");
                response.sendRedirect("/");
            }
        } catch (IOException e) {
            System.err.println("OAuth2 Login error: " + e.getMessage());
            response.sendRedirect("/login?error=oauth_error");
        }
    }

    private String getAssignedRoleForEmail(String email) {
        try {
            User existingUser = userService.findByEmailWithRole(email);
            if (existingUser != null && existingUser.getRole() != null) {
                String roleName = existingUser.getRole().getRoleName();
                if (!"Customer".equalsIgnoreCase(roleName)) {
                    return existingUser.getRoleId();
                }
            }
            
            if (isAdminEmail(email)) return roleService.getDefaultAdminRoleId();
            if (isStaffEmail(email)) return roleService.getDefaultStaffRoleId();
            if (isOwnerEmail(email)) return roleService.getDefaultOwnerRoleId();
            
            return null;
        } catch (Exception e) {
            System.err.println("Error checking assigned role for email " + email + ": " + e.getMessage());
            return null;
        }
    }
    
    private boolean isAdminEmail(String email) {
        return email != null && (email.equalsIgnoreCase("admin@ecodana.com") || email.endsWith("@ecodana.com"));
    }
    
    private boolean isStaffEmail(String email) {
        return email != null && email.equalsIgnoreCase("staff@ecodana.com");
    }
    
    private boolean isOwnerEmail(String email) {
        return email != null && email.equalsIgnoreCase("owner@ecodana.com");
    }

}
