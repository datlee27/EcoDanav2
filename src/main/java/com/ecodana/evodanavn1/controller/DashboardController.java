package com.ecodana.evodanavn1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;  // Để mock users cho admin

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.hasRole("Customer")) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookingService.getBookingsByUser(user));
        model.addAttribute("tab", tab != null ? tab : "bookings");
        return "dashboard";
    }

    @GetMapping("/staff")
    public String staff(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.hasRole("Staff")) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("bookings", bookingService.getAllBookings());  // Thêm method getAllBookings() trong BookingService
        model.addAttribute("tab", tab != null ? tab : "manage-vehicles");
        return "staff";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !user.hasRole("Admin")) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("users", getMockUsers());  // Mock list users
        model.addAttribute("tab", tab != null ? tab : "user-management");
        return "admin";
    }

    // Mock users cho admin
    private List<User> getMockUsers() {
        User user1 = new User();
        user1.setId("user1-id");
        user1.setUsername("john_doe");
        user1.setEmail("john@example.com");
        user1.setPassword("password123");
        user1.setPhoneNumber("0123456789");
        // Role will be set via roleId, not the role field
        
        User user2 = new User();
        user2.setId("user2-id");
        user2.setUsername("jane_smith");
        user2.setEmail("jane@example.com");
        user2.setPassword("password123");
        user2.setPhoneNumber("0987654321");
        // Role will be set via roleId, not the role field
        
        return List.of(user1, user2);
    }
}