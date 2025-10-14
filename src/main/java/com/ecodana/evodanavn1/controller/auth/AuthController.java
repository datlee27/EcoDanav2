package com.ecodana.evodanavn1.controller.auth;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.UserService.TokenValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.service.EmailService;
import com.ecodana.evodanavn1.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Inject AuthenticationManager to perform programmatic login
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null) {
            User userWithRole = userService.getUserWithRole(currentUser.getEmail());
            if (userWithRole != null) {
                session.setAttribute("currentUser", userWithRole);
                String roleName = userWithRole.getRoleName();
                System.out.println("Login success - User role: " + roleName);
                if ("Admin".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng Admin " + userWithRole.getFirstName() + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                    return "redirect:/admin";
                } else if ("Staff".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Bạn có thể quản lý đơn đặt xe và xe.");
                    return "redirect:/staff";
                } else if ("Owner".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                    return "redirect:/owner/dashboard";
                } else if ("Customer".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                    return "redirect:/";
                } else {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + " trở lại ecodana.");
                    return "redirect:/";
                }
            }
        }
        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng trở lại ecodana.");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid User user, BindingResult bindingResult,
                                      @RequestParam String confirmPassword, @RequestParam String phoneNumber,
                                      Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        phoneNumber = phoneNumber.trim();

        if (user.getPassword() != null && !user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Mật khẩu xác nhận không khớp.");
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email này đã được sử dụng.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/register";
        }

        try {
            String otp = generateOtp();
            emailService.sendOtpEmail(user.getEmail(), otp);

            user.setPhoneNumber(phoneNumber);
            String email = user.getEmail();
            String username = email.split("@")[0] + "_" + System.currentTimeMillis();
            user.setUsername(username);

            session.setAttribute("tempUser", user);
            session.setAttribute("otp", otp);
            session.setAttribute("otpTimestamp", System.currentTimeMillis());

            redirectAttributes.addFlashAttribute("email", user.getEmail());
            return "redirect:/verify-otp";

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Failed to send OTP email for: " + user.getEmail() + "; error: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("error", "Không thể gửi email OTP. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau.");
            return "auth/register";
        } catch (Exception e) {
            System.err.println("Exception during registration process: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("error", "Đã xảy ra lỗi hệ thống trong quá trình đăng ký. Vui lòng thử lại.");
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User tempUser = (User) session.getAttribute("tempUser");
        if (tempUser == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên của bạn đã hết hạn. Vui lòng bắt đầu lại quá trình đăng ký.");
            return "redirect:/register";
        }

        if (!model.containsAttribute("email")) {
            model.addAttribute("email", tempUser.getEmail());
        }
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String submittedOtp, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User tempUser = (User) session.getAttribute("tempUser");
        String storedOtp = (String) session.getAttribute("otp");
        Long otpTimestamp = (Long) session.getAttribute("otpTimestamp");

        if (tempUser == null || storedOtp == null || otpTimestamp == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên của bạn đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }

        if (System.currentTimeMillis() - otpTimestamp > 5 * 60 * 1000) { // 5 minutes validity
            redirectAttributes.addFlashAttribute("error", "Mã OTP đã hết hạn. Vui lòng đăng ký lại để nhận mã mới.");
            clearOtpSession(session);
            return "redirect:/register";
        }

        if (submittedOtp.equals(storedOtp)) {
            try {
                // Get the original (unencrypted) password from the session to log in
                String rawPassword = tempUser.getPassword();

                // 1. Save the user to the database (the password will be encrypted here)
                userService.register(tempUser);

                // 2. LOG THE USER INTO SPRING SECURITY
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(tempUser.getEmail(), rawPassword);
                Authentication authentication = authenticationManager.authenticate(authToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

                // 3. Get the full user information and save it to the session for immediate use
                User registeredUser = userService.getUserWithRole(tempUser.getEmail());
                session.setAttribute("currentUser", registeredUser);

                // Clear OTP data from the session
                clearOtpSession(session);

                // Redirect as before
                redirectAttributes.addFlashAttribute("success", "🎉 Đăng ký thành công! Chào mừng " + registeredUser.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                return "redirect:/";

            } catch (Exception e) {
                System.err.println("Failed to save user or auto-login after OTP verification: " + e.getMessage());
                model.addAttribute("error", "Đã xảy ra lỗi khi hoàn tất đăng ký. Vui lòng thử lại.");
                model.addAttribute("email", tempUser.getEmail());
                return "auth/verify-otp";
            }
        } else {
            model.addAttribute("error", "Mã OTP không hợp lệ. Vui lòng thử lại.");
            model.addAttribute("email", tempUser.getEmail());
            return "auth/verify-otp";
        }
    }

    @GetMapping("/profile")
    public String userProfile(Model model, HttpSession session) {
        // Get the User object saved in the session during login
        User currentUser = (User) session.getAttribute("currentUser");

        // If the user is not logged in (not in session), redirect to the login page
        if (currentUser == null) {
            return "redirect:/login";
        }

        // --- STEP 1: Provide information for the NAV bar ---
        // Add the currentUser object to the model so the nav bar can use it
        model.addAttribute("currentUser", currentUser);

        // --- STEP 2: Provide information for the PROFILE page content ---
        // Get the latest user information from the database based on the email or username in the session
        User userForProfile = userService.findByEmail(currentUser.getEmail());
        // Add to the model with the name "user" for profile.html to use
        model.addAttribute("user", userForProfile);

        // Return the view of the profile page
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateUserProfile(User user, RedirectAttributes redirectAttributes, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.updateUser(currentUser.getId(), user.getFirstName(), user.getLastName(), user.getUserDOB(), user.getGender() != null ? user.getGender().name() : null, user.getPhoneNumber());

        // Update the user's name in the session as well
        User updatedUser = userService.findByEmail(currentUser.getEmail());
        session.setAttribute("currentUser", updatedUser);

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("password_error", "Mật khẩu mới và mật khẩu xác nhận không khớp.");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("password_error", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/profile";
        }

        boolean isPasswordChanged = userService.changePasswordForAuthenticatedUser(currentUser.getId(), currentPassword, newPassword);

        if (isPasswordChanged) {
            redirectAttributes.addFlashAttribute("password_success", "Đổi mật khẩu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("password_error", "Mật khẩu hiện tại không đúng. Vui lòng thử lại.");
        }

        return "redirect:/profile";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = "Bạn";
        if (session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            username = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Tạm biệt, " + username + "! Bạn đã đăng xuất thành công.");
        return "redirect:/login";
    }

    private String generateOtp() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("tempUser");
        session.removeAttribute("otp");
        session.removeAttribute("otpTimestamp");
    }


    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String userEmail, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findUserByEmail(userEmail);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản nào với email này.");
            return "redirect:/forgot-password";
        }

        User user = userOptional.get();
        // The createPasswordResetTokenForUser method now handles deleting old tokens.
        var token = userService.createPasswordResetTokenForUser(user);

        String baseUrl = getBaseUrl(request);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken(), baseUrl);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi email. Vui lòng thử lại.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("message", "Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        TokenValidationResult result = userService.validatePasswordResetToken(token);
        if (result != TokenValidationResult.VALID) {
            String message = switch (result) {
                case EXPIRED -> "Liên kết đã hết hạn. Vui lòng yêu cầu một liên kết mới.";
                case USED -> "Liên kết đã được sử dụng. Vui lòng yêu cầu một liên kết mới.";
                default -> "Liên kết không hợp lệ. Vui lòng kiểm tra lại hoặc yêu cầu một liên kết mới.";
            };
            redirectAttributes.addFlashAttribute("error", message);
            return "redirect:/forgot-password";
        }

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("token", token);
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "redirect:/reset-password?token=" + token;
        }

        TokenValidationResult result = userService.resetPassword(token, newPassword);

        if (result == TokenValidationResult.VALID) {
            redirectAttributes.addFlashAttribute("message", "Mật khẩu của bạn đã được thay đổi thành công.");
            return "redirect:/login";
        } else {
            String message = switch (result) {
                case EXPIRED -> "Liên kết đã hết hạn. Vui lòng yêu cầu một liên kết mới.";
                case USED -> "Liên kết đã được sử dụng. Vui lòng yêu cầu một liên kết mới.";
                default -> "Liên kết không hợp lệ. Vui lòng thử lại.";
            };
            redirectAttributes.addFlashAttribute("error", message);
            return "redirect:/forgot-password";
        }
    }

    // Utility function to create baseUrl from the request
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        // Only add the port to the URL if it is not the default port (80 for http, 443 for https)
        if ((("http".equals(scheme) && serverPort == 80) || ("https".equals(scheme) && serverPort == 443))) {
            return scheme + "://" + serverName + contextPath;
        } else {
            return scheme + "://" + serverName + ":" + serverPort + contextPath;
        }
    }
}