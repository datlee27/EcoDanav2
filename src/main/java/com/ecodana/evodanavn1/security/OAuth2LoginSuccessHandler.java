package com.ecodana.evodanavn1.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            
            // Get user info from Google
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String sub = oauth2User.getAttribute("sub");
            
            if (email == null || email.isEmpty()) {
                response.sendRedirect("/login?error=no_email");
                return;
            }

            // Check if user already exists by email
            User existingUser = userService.findByEmail(email);
            User user;
            
            if (existingUser != null) {
                // User exists, use existing user
                user = existingUser;
            } else {
                // Create new user
                user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setUsername("google_" + (sub != null ? sub : email.split("@")[0]));
                user.setEmail(email);
                user.setPassword(""); // No password for OAuth users
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
                user.setRoleId("customer-role-id");
                user.setNormalizedUserName(user.getUsername().toUpperCase());
                user.setNormalizedEmail(user.getEmail().toUpperCase());
                user.setSecurityStamp(UUID.randomUUID().toString());
                user.setConcurrencyStamp(UUID.randomUUID().toString());
                user.setStatus("Active");
                user.setEmailVerified(true);
                user.setTwoFactorEnabled(false);
                user.setLockoutEnabled(false);
                user.setAccessFailedCount(0);
                user.setCreatedDate(java.time.LocalDateTime.now());
                
                // Save user to database
                userService.register(user);
            }

            // Set user in session
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);
            session.setAttribute("flash_success", "Đăng nhập bằng Google thành công!");
        }
        response.sendRedirect("/");
    }

    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
