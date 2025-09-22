package com.ecodana.evodanavn1.controller;

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
        return "user/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, @RequestParam(required = false) String secretKey, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username or email is required");
            return "user/login";
        }
        
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "user/login";
        }
        
        try {
            User user = userService.login(username.trim(), password, secretKey != null ? secretKey : "");
            if (user != null) {
                // Check if user is active
                if (!user.isActive()) {
                    model.addAttribute("error", "Your account has been deactivated. Please contact support.");
                    return "user/login";
                }
                
                session.setAttribute("currentUser", user);
                redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getUsername() + "!");
                
                // Redirect based on role
                if (User.Role.CUSTOMER.equals(user.getRole())) {
                    return "redirect:/dashboard";
                } else if (User.Role.STAFF.equals(user.getRole())) {
                    return "redirect:/staff";
                } else if (User.Role.ADMIN.equals(user.getRole())) {
                    return "redirect:/admin";
                } else {
                    return "redirect:/";
                }
            } else {
                model.addAttribute("error", "Invalid username/email or password. Please check your credentials and try again.");
                return "user/login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed due to a system error. Please try again later.");
            return "user/login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid User user, BindingResult bindingResult, @RequestParam String confirmPassword, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check password confirmation
        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Passwords do not match");
        }
        
        // Check if username already exists
        if (userService.findByUsername(user.getUsername()) != null) {
            bindingResult.rejectValue("username", "error.user", "Username already exists");
        }
        
        // Check if email already exists
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }
        
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        
        if (userService.register(user)) {
            session.setAttribute("currentUser", user);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Chào mừng bạn đến EvoDana.");
            return "redirect:/";
        }
        
        model.addAttribute("error", "Registration failed. Please try again.");
        return "user/register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = "User";
        if (session.getAttribute("currentUser") != null) {
            User user = (User) session.getAttribute("currentUser");
            username = user.getUsername();
        }
        
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Goodbye, " + username + "! You have been logged out successfully.");
        return "redirect:/";
    }
}