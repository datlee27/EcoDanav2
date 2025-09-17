package com.ecodana.evodanavn1.controller;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, @RequestParam(required = false) String secretKey, HttpSession session, Model model) {
        User user = userService.login(username, password, secretKey != null ? secretKey : "");
        if (user != null) {
            session.setAttribute("currentUser", user);
            if ("customer".equals(user.getRole())) {
                return "redirect:/dashboard";
            } else if ("staff".equals(user.getRole())) {
                return "redirect:/staff";
            } else {
                return "redirect:/admin";
            }
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username, @RequestParam String email, @RequestParam String phone, @RequestParam String password, @RequestParam String confirmPassword, Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }
        User newUser = new User("new", username, email, "customer", false); // Thêm phone nếu cần
        if (userService.register(newUser)) {
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        }
        model.addAttribute("error", "Registration failed");
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
