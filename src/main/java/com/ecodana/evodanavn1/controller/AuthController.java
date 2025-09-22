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
            System.out.println("=== AUTH CONTROLLER LOGIN ===");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            
            User user = userService.login(username.trim(), password, secretKey != null ? secretKey : "");
            System.out.println("User returned from service: " + (user != null ? user.getUsername() : "null"));
            
            if (user != null) {
                session.setAttribute("currentUser", user);
                redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getUsername() + "!");
                
                System.out.println("Login successful, redirecting to home");
                // Always redirect to home after login
                return "redirect:/";
            } else {
                System.out.println("User is null, login failed");
                model.addAttribute("error", "Invalid username/email or password. Please check your credentials and try again.");
                return "user/login";
            }
        } catch (Exception e) {
            System.out.println("Exception in login: " + e.getMessage());
            System.out.println("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
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
    public String doRegister(@Valid User user, BindingResult bindingResult, @RequestParam String confirmPassword, @RequestParam String phoneNumber, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check password confirmation
        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Passwords do not match");
        }
        
        // Check if email already exists
        if (userService.findByEmail(user.getEmail()) != null) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }
        
        // Validate required fields
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "error.user", "Email is required");
        }
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            bindingResult.rejectValue("phoneNumber", "error.user", "Phone number is required");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.user", "Password is required");
        }
        
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        
        // Set phone number from request parameter
        user.setPhoneNumber(phoneNumber);
        
        // Generate username from email
        String email = user.getEmail();
        String username = email.split("@")[0] + "_" + System.currentTimeMillis();
        user.setUsername(username);
        
        try {
            if (userService.register(user)) {
                session.setAttribute("currentUser", user);
                redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Chào mừng bạn đến EvoDana.");
                return "redirect:/";
            } else {
                System.err.println("Registration failed for user: " + user.getEmail());
                model.addAttribute("error", "Registration failed. Please try again.");
                return "user/register";
            }
        } catch (Exception e) {
            System.err.println("Exception in registration: " + e.getMessage());
            System.err.println("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            model.addAttribute("error", "Registration failed due to system error. Please try again.");
            return "user/register";
        }
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