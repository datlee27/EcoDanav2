package com.ecodana.evodanavn1.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.EmailService;
import com.ecodana.evodanavn1.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

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
                } else if ("Staff".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                    return "redirect:/owner/dashboard";
                } else if ("Customer".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                    return "redirect:/";
                } else {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + " trở lại EvoDana.");
                    return "redirect:/";
                }
            }
        }
        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng trở lại EvoDana.");
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

        // Sanitize and Validate input
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        phoneNumber = phoneNumber.trim();

        if (user.getPassword() != null && !user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Mật khẩu xác nhận không khớp.");
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email này đã được sử dụng.");
        }
        // Thêm các validation khác nếu cần

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "auth/register";
        }

        // Send OTP instead of saving user directly
        try {
            String otp = generateOtp();
            emailService.sendOtpEmail(user.getEmail(), otp);

            // Prepare user object to store temporarily
            user.setPhoneNumber(phoneNumber);
            String email = user.getEmail();
            String username = email.split("@")[0] + "_" + System.currentTimeMillis();
            user.setUsername(username);

            // Store temporary data in session
            session.setAttribute("tempUser", user);
            session.setAttribute("otp", otp);
            session.setAttribute("otpTimestamp", System.currentTimeMillis());

            // Redirect to OTP verification page
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
                // OTP is correct, now register the user permanently
                userService.register(tempUser);

                // Auto-login: Set user in session
                User registeredUser = userService.getUserWithRole(tempUser.getEmail());
                session.setAttribute("currentUser", registeredUser);

                // Clear OTP session data
                clearOtpSession(session);

                // Redirect based on user role
                String roleName = registeredUser.getRoleName();
                System.out.println("Registration and auto-login success - User role: " + roleName);
                if ("Admin".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng ký thành công! Chào mừng Admin " + registeredUser.getFirstName() + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                    return "redirect:/admin";
                } else if ("Staff".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng ký thành công! Chào mừng " + registeredUser.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                    return "redirect:/owner/dashboard";
                } else if ("Customer".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng ký thành công! Chào mừng " + registeredUser.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                    return "redirect:/";
                } else {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng ký thành công! Chào mừng " + registeredUser.getFirstName() + " trở lại EvoDana.");
                    return "redirect:/";
                }
            } catch (Exception e) {
                System.err.println("Failed to save user after OTP verification: " + e.getMessage());
                model.addAttribute("error", "Đã xảy ra lỗi khi lưu tài khoản của bạn. Vui lòng thử lại.");
                model.addAttribute("email", tempUser.getEmail());
                return "auth/verify-otp";
            }
        } else {
            model.addAttribute("error", "Mã OTP không hợp lệ. Vui lòng thử lại.");
            model.addAttribute("email", tempUser.getEmail());
            return "auth/verify-otp";
        }
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
}