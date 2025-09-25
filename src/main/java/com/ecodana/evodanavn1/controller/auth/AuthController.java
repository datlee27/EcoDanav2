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
import com.ecodana.evodanavn1.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Login attempt: " + username);
            
            // Use UserService.login() directly - it handles both username and email lookup
            User user = userService.login(username, password, null);
            
            if (user != null) {
                System.out.println("Login successful for: " + user.getEmail());
                
                // Reload user with role information
                User userWithRole = userService.getUserWithRole(user.getEmail());
                if (userWithRole != null) {
                    session.setAttribute("currentUser", userWithRole);
                    
                    // Redirect based on role
                    String roleName = userWithRole.getRoleName();
                    System.out.println("User role: " + roleName);
                    
                    if ("Admin".equalsIgnoreCase(roleName)) {
                        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng Admin " + userWithRole.getFirstName() + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                        return "redirect:/admin";
                    } else if ("Staff".equalsIgnoreCase(roleName)) {
                        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Bạn có thể quản lý xe và đặt chỗ.");
                        return "redirect:/owner/dashboard";
                    } else if ("Customer".equalsIgnoreCase(roleName)) {
                        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + "! Hãy khám phá và đặt xe ngay.");
                        return "redirect:/";
                    } else {
                        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng " + userWithRole.getFirstName() + " trở lại EvoDana.");
                        return "redirect:/";
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Login successful but unable to load user information. Please try again.");
                    return "redirect:/login";
                }
            } else {
                System.out.println("Login failed for: " + username);
                redirectAttributes.addFlashAttribute("error", "Invalid username or password.");
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred during login. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/login-success")
    public String loginSuccess(HttpSession session, RedirectAttributes redirectAttributes) {
        // This method will be called after successful Spring Security authentication
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser != null) {
            // Reload user with role information
            User userWithRole = userService.getUserWithRole(currentUser.getEmail());
            if (userWithRole != null) {
                session.setAttribute("currentUser", userWithRole);
                
                // Redirect based on role
                String roleName = userWithRole.getRoleName();
                System.out.println("Login success - User role: " + roleName);
                
                if ("Admin".equalsIgnoreCase(roleName)) {
                    redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng Admin " + userWithRole.getFirstName() + "! Bạn có quyền truy cập đầy đủ hệ thống.");
                    return "redirect:/admin";
                } else if ("Staff".equalsIgnoreCase(roleName)) {
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
        
        // Fallback to home page if no user or role found
        redirectAttributes.addFlashAttribute("success", "🎉 Đăng nhập thành công! Chào mừng trở lại EvoDana.");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid User user, BindingResult bindingResult, @RequestParam String confirmPassword, @RequestParam String phoneNumber, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Sanitize input
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        if (phoneNumber != null) {
            phoneNumber = phoneNumber.trim();
        }
        if (confirmPassword != null) {
            confirmPassword = confirmPassword.trim();
        }
        
        // Check password confirmation
        if (user.getPassword() != null && confirmPassword != null && !user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Passwords do not match");
        }
        
        // Check if email already exists
        if (user.getEmail() != null && userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }
        
        // Validate required fields
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "error.user", "Email is required");
        }
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            bindingResult.rejectValue("phoneNumber", "error.user", "Phone number is required");
        } else if (!phoneNumber.matches("^[0-9]*$")) {
            bindingResult.rejectValue("phoneNumber", "error.user", "Phone number must contain only digits");
        } else if (phoneNumber.length() > 11) {
            bindingResult.rejectValue("phoneNumber", "error.user", "Phone number must not exceed 11 digits");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.user", "Password is required");
        } else if (user.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "error.user", "Password must be at least 6 characters");
        }
        
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // Set phone number from request parameter
        user.setPhoneNumber(phoneNumber);
        
        // Generate username from email
        String email = user.getEmail();
        String username = email.split("@")[0] + "_" + System.currentTimeMillis();
        user.setUsername(username);
        
        try {
            if (userService.register(user)) {
                // Reload user with role information
                User savedUser = userService.findByEmailWithRole(user.getEmail());
                if (savedUser != null) {
                    session.setAttribute("currentUser", savedUser);
                    redirectAttributes.addFlashAttribute("success", "Registration successful! Welcome to EvoDana.");
                    return "redirect:/";
                } else {
                    System.err.println("Failed to reload user after registration: " + user.getEmail());
                    model.addAttribute("error", "Registration completed but failed to load user data. Please try logging in.");
                    return "auth/register";
                }
            } else {
                System.err.println("Registration failed for user: " + user.getEmail());
                model.addAttribute("error", "Registration failed. Please try again.");
                return "auth/register";
            }
        } catch (Exception e) {
            System.err.println("Exception in registration: " + e.getMessage());
            model.addAttribute("error", "Registration failed due to system error. Please try again.");
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = "User";
        if (session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            username = user.getUsername();
        }
        
        // Clear all session attributes before invalidating
        session.removeAttribute("currentUser");
        session.removeAttribute("user");
        session.removeAttribute("userId");
        session.removeAttribute("username");
        session.removeAttribute("email");
        session.removeAttribute("SPRING_SECURITY_CONTEXT");
        session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
        
        // Invalidate the entire session
        session.invalidate();
        
        redirectAttributes.addFlashAttribute("success", "Goodbye, " + username + "! You have been logged out successfully.");
        return "redirect:/";
    }
}