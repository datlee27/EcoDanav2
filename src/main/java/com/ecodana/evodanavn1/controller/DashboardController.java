package com.ecodana.evodanavn1.controller;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
        if (user == null || !"customer".equals(user.getRole())) {
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
        if (user == null || !"staff".equals(user.getRole())) {
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
        if (user == null || !"admin".equals(user.getRole())) {
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
        return List.of(
                new User("1", "john_doe", "john@example.com", "customer", true),
                new User("2", "jane_smith", "jane@example.com", "staff", true)
        );
    }
}