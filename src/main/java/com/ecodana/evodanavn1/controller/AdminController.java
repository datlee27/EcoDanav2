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
public class AdminController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(required = false) String tab, HttpSession session, Model model) {
        System.out.println("Admin dashboard accessed");
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            System.out.println("No user in session, redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("User found in session: " + user.getEmail());
        
        // Reload user with role information
        User userWithRole = userService.getUserWithRole(user.getEmail());
        if (userWithRole == null) {
            System.out.println("Could not load user with role, redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("User role: " + userWithRole.getRoleName());
        
        // Check if user has admin role
        if (!userService.isAdmin(userWithRole)) {
            System.out.println("User is not admin, redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("User is admin, loading dashboard data");
        
        try {
            // Load admin-specific data from database
            System.out.println("Loading vehicles...");
            var allVehicles = vehicleService.getAllVehicles();
            System.out.println("Vehicles loaded: " + allVehicles.size());
            
            System.out.println("Loading bookings...");
            var allBookings = bookingService.getAllBookings();
            System.out.println("Bookings loaded: " + allBookings.size());
            
            System.out.println("Loading users...");
            var allUsers = userService.getAllUsers();
            System.out.println("Users loaded: " + allUsers.size());
            
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);  // Add currentUser for template
            model.addAttribute("vehicles", allVehicles);
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("bookings", allBookings);
            model.addAttribute("totalBookings", allBookings.size());
            model.addAttribute("users", allUsers);
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalRevenue", bookingService.getTotalRevenue());
            model.addAttribute("activeSessions", 1); // Mock active sessions
            model.addAttribute("tab", tab != null ? tab : "overview");
            
            System.out.println("Returning admin/admin-dashboard template");
            return "admin/admin-dashboard";
        } catch (Exception e) {
            System.out.println("Error loading admin data: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to mock data if database is not available
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);  // Add currentUser for template
            model.addAttribute("vehicles", List.of());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("bookings", List.of());
            model.addAttribute("totalBookings", 0);
            model.addAttribute("users", List.of());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("activeSessions", 1);
            model.addAttribute("tab", tab != null ? tab : "overview");
            
            System.out.println("Returning admin/admin-dashboard template with fallback data");
            return "admin/admin-dashboard";
        }
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        var allBookings = bookingService.getAllBookings();
        var pendingBookings = bookingService.getPendingBookings();
        var confirmedBookings = bookingService.getActiveBookings();
        
        model.addAttribute("bookings", allBookings);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", pendingBookings.size());
        model.addAttribute("confirmedBookings", confirmedBookings.size());
        model.addAttribute("cancelledBookings", 0); // Mock data
        
        return "admin/bookings-management";
    }

    @GetMapping("/admin/vehicles")
    public String adminVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        var allVehicles = vehicleService.getAllVehicles();
        var availableVehicles = vehicleService.getAvailableVehicles();
        
        model.addAttribute("vehicles", allVehicles);
        model.addAttribute("totalVehicles", allVehicles.size());
        model.addAttribute("availableVehicles", availableVehicles.size());
        model.addAttribute("inUseVehicles", 0); // Mock data
        model.addAttribute("maintenanceVehicles", 0); // Mock data
        
        return "admin/vehicles-management";
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        var allUsers = userService.getAllUsers();
        
        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("activeUsers", allUsers.size()); // Mock data
        model.addAttribute("pendingUsers", 0); // Mock data
        model.addAttribute("suspendedUsers", 0); // Mock data
        
        return "admin/users-management";
    }

    @GetMapping("/admin/reports")
    public String adminReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        var allBookings = bookingService.getAllBookings();
        var allUsers = userService.getAllUsers();
        
        model.addAttribute("totalRevenue", bookingService.getTotalRevenue());
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("activeUsers", allUsers.size());
        
        return "admin/reports";
    }
}
