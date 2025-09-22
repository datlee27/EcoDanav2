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
                user.setRole(User.Role.CUSTOMER);
                user.setHasLicense(false);
                user.setActive(true);
                
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
