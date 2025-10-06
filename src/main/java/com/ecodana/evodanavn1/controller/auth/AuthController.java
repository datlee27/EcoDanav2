package com.ecodana.evodanavn1.controller.auth;

import com.ecodana.evodanavn1.model.PasswordResetToken;
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

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.EmailService;
import com.ecodana.evodanavn1.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Inject AuthenticationManager để thực hiện đăng nhập theo chương trình
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
                } else if ("Staff".equalsIgnoreCase(roleName) || "Owner".equalsIgnoreCase(roleName)) {
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
                // Lấy mật khẩu gốc (chưa mã hóa) từ session để đăng nhập
                String rawPassword = tempUser.getPassword();

                // 1. Lưu người dùng vào CSDL (mật khẩu sẽ được mã hóa tại đây)
                userService.register(tempUser);

                // 2. ĐĂNG NHẬP NGƯỜI DÙNG VÀO SPRING SECURITY
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(tempUser.getEmail(), rawPassword);
                Authentication authentication = authenticationManager.authenticate(authToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

                // 3. Lấy thông tin đầy đủ của người dùng và lưu vào session để sử dụng ngay
                User registeredUser = userService.getUserWithRole(tempUser.getEmail());
                session.setAttribute("currentUser", registeredUser);

                // Xóa dữ liệu OTP khỏi session
                clearOtpSession(session);

                // Chuyển hướng như cũ
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
        // Lấy đối tượng User đã được lưu trong session khi đăng nhập
        User currentUser = (User) session.getAttribute("currentUser");

        // Nếu người dùng chưa đăng nhập (không có trong session), chuyển hướng về trang đăng nhập
        if (currentUser == null) {
            return "redirect:/login";
        }

        // --- BƯỚC 1: Cung cấp thông tin cho thanh NAV ---\
        // Thêm đối tượng currentUser vào model để thanh nav có thể sử dụng
        model.addAttribute("currentUser", currentUser);

        // --- BƯỚC 2: Cung cấp thông tin cho nội dung trang PROFILE ---\
        // Lấy thông tin mới nhất của người dùng từ CSDL dựa trên email hoặc username trong session
        User userForProfile = userService.findByEmail(currentUser.getEmail());
        // Thêm vào model với tên "user" để profile.html sử dụng
        model.addAttribute("user", userForProfile);

        // Trả về view của trang profile
        return "auth/profile";
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
        PasswordResetToken token = userService.createPasswordResetTokenForUser(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken(), request);
        } catch (MessagingException | UnsupportedEncodingException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi email. Vui lòng thử lại.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("message", "Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            String message = switch (result) {
                case "expired" -> "Liên kết đã hết hạn. Vui lòng yêu cầu một liên kết mới.";
                case "usedToken" -> "Liên kết đã được sử dụng. Vui lòng yêu cầu một liên kết mới.";
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

        String result = userService.validatePasswordResetToken(token);
        if (result != null) {
            redirectAttributes.addFlashAttribute("error", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("token", token);
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "redirect:/reset-password?token=" + token;
        }

        Optional<PasswordResetToken> tokenOptional = userService.getPasswordResetToken(token);
        if(tokenOptional.isPresent()){
            PasswordResetToken resetToken = tokenOptional.get();
            User user = resetToken.getUser();
            userService.changeUserPassword(user, newPassword);

            // Đánh dấu token đã được sử dụng
            resetToken.setUsed(true);
            // Cần có phương thức save trong repository
            // tokenRepository.save(resetToken);

            redirectAttributes.addFlashAttribute("message", "Mật khẩu của bạn đã được thay đổi thành công.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi. Vui lòng thử lại.");
            return "redirect:/forgot-password";
        }
    }
}