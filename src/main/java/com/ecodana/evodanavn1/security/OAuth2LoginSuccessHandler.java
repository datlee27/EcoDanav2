package com.ecodana.evodanavn1.security;

import java.io.IOException;
import java.util.UUID;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // Import mới
import org.springframework.security.crypto.password.PasswordEncoder; // Import mới
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

    // === BẮT ĐẦU SỬA LỖI ===
    // 1. Xóa @Autowired khỏi các trường và khai báo là 'final'
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    // 2. Thêm Constructor Injection
    @Autowired
    public OAuth2LoginSuccessHandler(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }
    // === KẾT THÚC SỬA LỖI ===

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            Object principal = authentication.getPrincipal();
            User user; // User từ DB

            // Lấy thông tin OIDC
            String loginProvider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            OAuth2User oauth2User;

            if (principal instanceof CustomOidcUser customOidcUser) {
                user = customOidcUser.getUser(); // Có thể là null nếu user mới/chưa liên kết
                oauth2User = customOidcUser; // CustomOidcUser cũng là một OAuth2User
            } else if (principal instanceof OAuth2User) {
                // Luồng dự phòng (ít khi xảy ra nếu CustomOAuth2UserService đã chạy)
                oauth2User = (OAuth2User) principal;
                String providerKey = oauth2User.getName(); // Lấy providerKey (subject)
                user = userService.findUserByLogin(loginProvider, providerKey).orElse(null);
            } else {
                response.sendRedirect("/login?error=invalid_principal");
                return;
            }

            // === SỬA LỖI LOGIC (NullPointerException) ===
            // Nếu 'user' là null (tức là user mới hoặc chưa liên kết),
            // chúng ta phải tạo mới hoặc liên kết ngay tại đây.
            if (user == null) {
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String avatarUrl = oauth2User.getAttribute("picture");
                String providerKey = oauth2User.getName(); // ID duy nhất từ Google (subject)

                if (email == null || email.isEmpty()) {
                    response.sendRedirect("/login?error=no_email");
                    return;
                }

                // Kiểm tra xem email đã tồn tại (đăng ký bằng password) chưa
                User existingUser = userService.findByEmail(email);

                if (existingUser != null) {
                    // 1. LIÊN KẾT TÀI KHOẢN
                    user = existingUser;
                    userService.linkOAuthAccount(user, loginProvider, providerKey, email);
                    System.out.println("OAuthSuccessHandler: Đã liên kết " + loginProvider + " với người dùng (email): " + email);
                } else {
                    // 2. TẠO TÀI KHOẢN MỚI
                    user = new User();
                    user.setId(UUID.randomUUID().toString());
                    user.setEmail(email);
                    // Mã hóa một mật khẩu ngẫu nhiên an toàn cho tài khoản chỉ dùng OAuth
                    user.setPassword(passwordEncoder.encode("OAUTH_USER_" + UUID.randomUUID().toString()));
                    user.setPhoneNumber(""); // Bắt buộc (theo logic cũ)
                    user.setStatus(User.UserStatus.Active);

                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        user.setFirstName(nameParts.length > 0 ? nameParts[0] : name);
                        user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                    } else {
                        user.setFirstName(email.split("@")[0]);
                        user.setLastName("");
                    }

                    // Tạo username từ email + timestamp để đảm bảo duy nhất
                    user.setUsername(email.split("@")[0] + "_" + System.currentTimeMillis());
                    user.setAvatarUrl(avatarUrl); // Đặt ảnh đại diện

                    String assignedRoleId = getAssignedRoleForEmail(email);
                    user.setRoleId(assignedRoleId != null ? assignedRoleId : roleService.getDefaultCustomerRoleId());

                    // Các trường bắt buộc khác
                    user.setNormalizedUserName(user.getUsername().toUpperCase());
                    user.setNormalizedEmail(user.getEmail().toUpperCase());
                    user.setSecurityStamp(UUID.randomUUID().toString());
                    user.setConcurrencyStamp(UUID.randomUUID().toString());
                    user.setEmailVerifed(true); // Email từ Google/OIDC được coi là đã xác thực
                    user.setCreatedDate(java.time.LocalDateTime.now());

                    // Lưu User mới (Dùng save() thay vì register() để tránh mã hóa kép)
                    user = userService.save(user);

                    // Liên kết tài khoản OAuth
                    userService.linkOAuthAccount(user, loginProvider, providerKey, email);
                    System.out.println("OAuthSuccessHandler: Đã tạo người dùng mới bằng " + loginProvider + ": " + email);
                }
            }
            // === KẾT THÚC SỬA LỖI LOGIC ===

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

            // Tải lại user với thông tin Role đầy đủ (quan trọng)
            User userWithRole = userService.findByIdWithRole(user.getId());
            session.setAttribute("currentUser", userWithRole);

            String roleName = userWithRole.getRoleName();
            String displayName = userWithRole.getFirstName() != null ? userWithRole.getFirstName() : userWithRole.getUsername();

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