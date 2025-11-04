package com.ecodana.evodanavn1.security;

import java.io.IOException;
import java.util.UUID;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
            } else if (principal instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");

                if (email == null || email.isEmpty()) {
                    response.sendRedirect("/login?error=no_email");
                    return;
                }

                User existingUser = userService.findByEmail(email);

                if (existingUser != null) {
                    user = existingUser;
                } else {
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setEmail(email);
                    user.setPassword("OAUTH_USER_" + UUID.randomUUID().toString());
                    user.setPhoneNumber("");
                    user.setStatus(User.UserStatus.Active);

                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
                        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                    }
                    user.setUsername(user.getFirstName() + user.getLastName());

                    String assignedRoleId = getAssignedRoleForEmail(email);
                    user.setRoleId(assignedRoleId != null ? assignedRoleId : roleService.getDefaultCustomerRoleId());

                    user.setNormalizedUserName(user.getUsername().toUpperCase());
                    user.setNormalizedEmail(user.getEmail().toUpperCase());
                    user.setSecurityStamp(UUID.randomUUID().toString());
                    user.setConcurrencyStamp(UUID.randomUUID().toString());
                    user.setEmailVerifed(true);
                    user.setCreatedDate(java.time.LocalDateTime.now());

                    if (!userService.register(user)) {
                        response.sendRedirect("/login?error=registration_failed");
                        return;
                    }
                }
            } else {
                response.sendRedirect("/login?error=invalid_principal");
                return;
            }

            // Check if user is banned or inactive
            if (user.getStatus() == User.UserStatus.Banned) {
                response.sendRedirect("/login?error=account_banned");
                return;
            }
            
            if (user.getStatus() == User.UserStatus.Inactive) {
                response.sendRedirect("/login?error=account_inactive");
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", user);

            String roleName = user.getRoleName();
            String displayName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();

            if ("Admin".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập thành công! Chào mừng Admin " + displayName + "!");
                response.sendRedirect("/admin");
            } else if ("Owner".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập thành công! Chào mừng Owner " + displayName + "!");
                response.sendRedirect("/owner/dashboard");
            } else if ("Staff".equalsIgnoreCase(roleName)) {
                session.setAttribute("flash_success", "🎉 Đăng nhập thành công! Chào mừng Staff " + displayName + "!");
                response.sendRedirect("/staff");
            } else {
                session.setAttribute("flash_success", "🎉 Đăng nhập thành công! Chào mừng " + displayName + "!");
                response.sendRedirect("/");
            }
        } catch (IOException e) {
            response.sendRedirect("/login?error=oauth_error");
        }
    }

    private String getAssignedRoleForEmail(String email) {
        try {
            User existingUser = userService.findByEmailWithRole(email);
            if (existingUser != null && existingUser.getRole() != null && !"Customer".equalsIgnoreCase(existingUser.getRole().getRoleName())) {
                return existingUser.getRoleId();
            }
            if (isAdminEmail(email)) return roleService.getDefaultAdminRoleId();
            if (isStaffEmail(email)) return roleService.getDefaultStaffRoleId();
            if (isOwnerEmail(email)) return roleService.getDefaultOwnerRoleId();
            return null;
        } catch (Exception e) {
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