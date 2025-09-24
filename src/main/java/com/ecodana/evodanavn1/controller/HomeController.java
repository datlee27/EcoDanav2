package com.ecodana.evodanavn1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        // If user is logged in, show home page with user info
        if (currentUser != null) {
            // Reload user with role information
            User userWithRole = userService.getUserWithRole(currentUser.getEmail());
            if (userWithRole != null) {
                currentUser = userWithRole;
                session.setAttribute("currentUser", currentUser);
            }
        }
        
        // Always add currentUser to model (null if not logged in)
        model.addAttribute("currentUser", currentUser);
        
        // Get featured vehicles (limit to 3 or available vehicles)
        var allVehicles = vehicleService.getAllVehicles();
        var featuredVehicles = allVehicles.size() >= 3 ? allVehicles.subList(0, 3) : allVehicles;
        model.addAttribute("vehicles", featuredVehicles);
        return "home";
    }
    
    /**
     * Redirect to appropriate dashboard based on user role
     * @param user the current user
     * @return redirect URL
     */
    private String redirectToDashboard(User user) {
        if (userService.isAdmin(user)) {
            return "redirect:/admin";
        } else if (userService.isStaff(user)) {
            return "redirect:/owner";
        } else if (userService.isCustomer(user)) {
            return "redirect:/dashboard";
        } else {
            // Default to customer dashboard if role is unknown
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "vehicles";
    }

    @GetMapping("/vehicles/detail")
    public String vehicleDetail(@RequestParam String id, Model model) {
        var vehicle = vehicleService.getVehicleById(id);
        if (vehicle.isPresent()) {
            model.addAttribute("vehicle", vehicle.get());
            return "vehicle-detail";
        } else {
            return "redirect:/vehicles?error=Vehicle not found";
        }
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation(Model model) {
        // Mock summary
        model.addAttribute("bookingSummary", "Tesla Model 3 - Dec 15-18, 2024 - $267");
        return "booking-confirmation";
    }
}