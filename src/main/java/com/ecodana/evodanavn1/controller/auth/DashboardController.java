package com.ecodana.evodanavn1.controller.auth;

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
        System.out.println("DashboardController - Session ID: " + session.getId());
        System.out.println("DashboardController - User in session: " + (user != null));
        if (user != null) {
            System.out.println("DashboardController - User email: " + user.getEmail());
            System.out.println("DashboardController - User role: " + user.getRoleName());
        }
        if (user == null) {
            System.out.println("DashboardController - No user in session, redirecting to login");
            return "redirect:/login";
        }
        
        // Reload user with role information
        User userWithRole = userService.getUserWithRole(user.getEmail());
        if (userWithRole == null) {
            return "redirect:/login";
        }
        
        // Check if user has customer role
        if (!userService.isCustomer(userWithRole)) {
            return "redirect:/login";
        }
        
        try {
            // Load customer-specific data from database
            var userBookings = bookingService.getBookingsByUser(userWithRole);
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);  // Add currentUser for template
            model.addAttribute("bookings", userBookings);
            model.addAttribute("activeBookings", bookingService.getActiveBookingsByUser(userWithRole));
            model.addAttribute("totalBookings", userBookings.size());
            model.addAttribute("favoriteVehicles", vehicleService.getFavoriteVehiclesByUser(userWithRole));
            model.addAttribute("reviewsGiven", bookingService.getReviewsByUser(userWithRole));
            model.addAttribute("tab", tab != null ? tab : "bookings");
            
            return "customer/customer-dashboard";
        } catch (Exception e) {
            // Fallback to empty data if database is not available
            model.addAttribute("user", userWithRole);
            model.addAttribute("bookings", List.of());
            model.addAttribute("activeBookings", List.of());
            model.addAttribute("totalBookings", List.of());
            model.addAttribute("favoriteVehicles", List.of());
            model.addAttribute("reviewsGiven", List.of());
            model.addAttribute("tab", tab != null ? tab : "bookings");
            
            return "customer/customer-dashboard";
        }
    }

    @GetMapping("/staff")
    public String staff(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Reload user with role information
        User userWithRole = userService.getUserWithRole(user.getEmail());
        if (userWithRole == null) {
            return "redirect:/login";
        }
        
        // Check if user has staff role
        if (!userService.isStaff(userWithRole)) {
            return "redirect:/login";
        }
        
        try {
            // Load staff-specific data from database
            var allVehicles = vehicleService.getAllVehicles();
            var allBookings = bookingService.getAllBookings();
            model.addAttribute("user", userWithRole);
            model.addAttribute("vehicles", allVehicles);
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("bookings", allBookings);
            model.addAttribute("activeBookings", bookingService.getActiveBookings());
            model.addAttribute("pendingBookings", bookingService.getPendingBookings());
            model.addAttribute("todayRevenue", bookingService.getTodayRevenue());
            model.addAttribute("tab", tab != null ? tab : "vehicles");
            
            return "staff/staff-dashboard";
        } catch (Exception e) {
            // Fallback to empty data if database is not available
            model.addAttribute("user", userWithRole);
            model.addAttribute("vehicles", List.of());
            model.addAttribute("totalVehicles", List.of());
            model.addAttribute("bookings", List.of());
            model.addAttribute("activeBookings", List.of());
            model.addAttribute("pendingBookings", List.of());
            model.addAttribute("todayRevenue", 0.0);
            model.addAttribute("tab", tab != null ? tab : "vehicles");
            
            return "staff/staff-dashboard";
        }
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