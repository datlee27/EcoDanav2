package com.ecodana.evodanavn1.controller.owner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private BookingService bookingService;

    /**
     * Helper method to check if user is authenticated and has owner/staff/admin role
     */
    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        
        // Add currentUser to model for template
        model.addAttribute("currentUser", currentUser);
        
        if (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Owner/Staff role required.");
            return "redirect:/login";
        }
        
        return null; // No redirect needed
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check authentication
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        
        User currentUser = (User) session.getAttribute("currentUser");
        
        // Reload user with role information
        User userWithRole = userService.getUserWithRole(currentUser.getEmail());
        if (userWithRole == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        
        try {
            // Load real data from services
            var allVehicles = vehicleService.getAllVehicles();
            var allBookings = bookingService.getAllBookings();
            
            model.addAttribute("user", userWithRole);
            model.addAttribute("vehicles", allVehicles);
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("bookings", allBookings);
            model.addAttribute("activeBookings", bookingService.getActiveBookings());
            model.addAttribute("pendingBookings", bookingService.getPendingBookings());
            model.addAttribute("todayRevenue", bookingService.getTodayRevenue());
            
            return "owner/dashboard";
        } catch (Exception e) {
            // Fallback to mock data if database is not available
            Map<String, Object> owner = new HashMap<>();
            owner.put("firstName", userWithRole.getFirstName() != null ? userWithRole.getFirstName() : "John");
            owner.put("name", userWithRole.getFirstName() != null ? userWithRole.getFirstName() + " " + userWithRole.getLastName() : "John Smith");
            owner.put("avatar", "/images/john-smith.jpg");
            owner.put("email", userWithRole.getEmail());

            // Mock dashboard stats
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCars", 12);
            stats.put("carsGrowth", "+2 this month");
            stats.put("monthlyIncome", "8450");
            stats.put("incomeGrowth", "+15% vs last month");
            stats.put("activeBookings", 28);
            stats.put("endingToday", "5 ending today");
            stats.put("availableCars", 7);

            // Mock recent bookings
            List<Map<String, Object>> recentBookings = new ArrayList<>();
            Map<String, Object> booking1 = new HashMap<>();
            booking1.put("carModel", "BMW X5");
            booking1.put("customerName", "Sarah Johnson");
            booking1.put("dateRange", "Feb 15-18, 2025");
            booking1.put("dailyRate", 320);
            booking1.put("status", "Active");
            booking1.put("statusClass", "bg-green-100 text-green-600");
            recentBookings.add(booking1);

            Map<String, Object> booking2 = new HashMap<>();
            booking2.put("carModel", "Tesla Model 3");
            booking2.put("customerName", "Mike Brown");
            booking2.put("dateRange", "Feb 20-22, 2025");
            booking2.put("dailyRate", 280);
            booking2.put("status", "Pending");
            booking2.put("statusClass", "bg-yellow-100 text-yellow-600");
            recentBookings.add(booking2);

            // Mock cars
            List<Map<String, Object>> cars = new ArrayList<>();
            Map<String, Object> car1 = new HashMap<>();
            car1.put("id", 1L);
            car1.put("brand", "BMW");
            car1.put("model", "X5");
            car1.put("imageUrl", "https://via.placeholder.com/300x200/1572D3/FFFFFF?text=BMW+X5");
            car1.put("rating", 4.9);
            car1.put("seats", 5);
            car1.put("transmission", "Automatic");
            car1.put("fuelType", "Petrol");
            car1.put("year", 2023);
            car1.put("dailyRate", 320);
            car1.put("status", "Available");
            car1.put("statusClass", "bg-green-100 text-green-600");
            cars.add(car1);

            Map<String, Object> car2 = new HashMap<>();
            car2.put("id", 2L);
            car2.put("brand", "Tesla");
            car2.put("model", "Model 3");
            car2.put("imageUrl", "https://via.placeholder.com/300x200/1572D3/FFFFFF?text=Tesla+Model+3");
            car2.put("rating", 4.7);
            car2.put("seats", 5);
            car2.put("transmission", "Automatic");
            car2.put("fuelType", "Electric");
            car2.put("year", 2022);
            car2.put("dailyRate", 280);
            car2.put("status", "Booked");
            car2.put("statusClass", "bg-red-100 text-red-600");
            cars.add(car2);

            // Mock all bookings
            List<Map<String, Object>> allBookings = new ArrayList<>();
            Map<String, Object> booking3 = new HashMap<>();
            booking3.put("id", 1L);
            booking3.put("customer", Map.of("name", "Sarah Johnson", "email", "sarah@example.com", "avatar", "/images/sarah.jpg"));
            booking3.put("car", Map.of("brand", "BMW", "model", "X5", "type", "SUV", "year", 2023));
            booking3.put("dateRange", "Feb 15-18, 2025");
            booking3.put("duration", 3);
            booking3.put("totalAmount", 960);
            booking3.put("status", "Active");
            booking3.put("statusClass", "bg-green-100 text-green-600");
            allBookings.add(booking3);

            // Mock income data
            model.addAttribute("weeklyIncome", "2150");
            model.addAttribute("monthlyIncome", "8450");
            model.addAttribute("yearlyIncome", "95200");

            // Mock notification count
            model.addAttribute("notificationCount", 3);

            // Add attributes to model
            model.addAttribute("stats", stats);
            model.addAttribute("owner", owner);
            model.addAttribute("recentBookings", recentBookings);
            model.addAttribute("cars", cars);
            model.addAttribute("bookings", allBookings);

            return "owner/dashboard";
        }
    }

    @PostMapping("/cars")
    public String addCar(@RequestParam Map<String, String> carData,
                         @RequestParam(value = "images", required = false) MultipartFile[] images,
                         HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        // Check authentication
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        
        try {
            // Create new vehicle from form data
            com.ecodana.evodanavn1.model.Vehicle vehicle = new com.ecodana.evodanavn1.model.Vehicle();
            vehicle.setVehicleId(java.util.UUID.randomUUID().toString());
            vehicle.setVehicleModel(carData.get("model"));
            vehicle.setVehicleType(carData.get("type"));
            vehicle.setLicensePlate(carData.get("licensePlate"));
            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));
            vehicle.setPricePerDay(new java.math.BigDecimal(carData.getOrDefault("dailyRate", "0")));
            vehicle.setPricePerHour(new java.math.BigDecimal(carData.getOrDefault("hourlyRate", "0")));
            vehicle.setPricePerMonth(new java.math.BigDecimal(carData.getOrDefault("monthlyRate", "0")));
            
            // Handle battery capacity conversion
            if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
            }
            
            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
            vehicle.setStatus("Available");
            vehicle.setCreatedDate(java.time.LocalDateTime.now());
            
            vehicleService.saveVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle added successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add vehicle: " + e.getMessage());
        }
        
        return "redirect:/owner/dashboard?section=cars";
    }

    @PutMapping("/cars/{id}")
    public String updateCar(@PathVariable String id,
                            @RequestParam Map<String, String> carData,
                            @RequestParam(value = "images", required = false) MultipartFile[] images,
                            HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        // Check authentication
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        
        try {
            var vehicleOpt = vehicleService.getVehicleById(id);
            if (vehicleOpt.isPresent()) {
                var vehicle = vehicleOpt.get();
                vehicle.setVehicleModel(carData.get("model"));
                vehicle.setVehicleType(carData.get("type"));
                vehicle.setLicensePlate(carData.get("licensePlate"));
                vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
                vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));
                vehicle.setPricePerDay(new java.math.BigDecimal(carData.getOrDefault("dailyRate", "0")));
                vehicle.setPricePerHour(new java.math.BigDecimal(carData.getOrDefault("hourlyRate", "0")));
                vehicle.setPricePerMonth(new java.math.BigDecimal(carData.getOrDefault("monthlyRate", "0")));
                
                // Handle battery capacity conversion
                if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                    vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
                }
                
                vehicle.setDescription(carData.get("description"));
                vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
                
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update vehicle: " + e.getMessage());
        }
        
        return "redirect:/owner/dashboard?section=cars";
    }

    @DeleteMapping("/cars/{id}")
    @ResponseBody
    public Map<String, Object> deleteCar(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }
        
        try {
            vehicleService.deleteVehicle(id);
            response.put("status", "success");
            response.put("message", "Vehicle deleted successfully");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete vehicle: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/bookings/{id}/accept")
    @ResponseBody
    public Map<String, Object> acceptBooking(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }
        
        try {
            var bookingOpt = bookingService.findById(id);
            if (bookingOpt.isPresent()) {
                var booking = bookingOpt.get();
                booking.setStatus("Confirmed");
                bookingService.updateBooking(booking);
                response.put("status", "success");
                response.put("message", "Booking accepted successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to accept booking: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/bookings/{id}/decline")
    @ResponseBody
    public Map<String, Object> declineBooking(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }
        
        try {
            var bookingOpt = bookingService.findById(id);
            if (bookingOpt.isPresent()) {
                var booking = bookingOpt.get();
                booking.setStatus("Cancelled");
                bookingService.updateBooking(booking);
                response.put("status", "success");
                response.put("message", "Booking declined successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to decline booking: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam Map<String, String> ownerUpdate, 
                               HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        // Check authentication
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            User userWithRole = userService.getUserWithRole(currentUser.getEmail());
            
            if (userWithRole != null) {
                userWithRole.setFirstName(ownerUpdate.get("firstName"));
                userWithRole.setLastName(ownerUpdate.get("lastName"));
                userWithRole.setPhoneNumber(ownerUpdate.get("phoneNumber"));
                // Address field removed - not in database
                
                userService.updateUser(userWithRole);
                session.setAttribute("currentUser", userWithRole);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/owner/dashboard?section=profile";
    }

    @GetMapping("/cars/{id}/edit")
    @ResponseBody
    public Map<String, Object> getCarForEdit(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }
        
        try {
            var vehicleOpt = vehicleService.getVehicleById(id);
            if (vehicleOpt.isPresent()) {
                var vehicle = vehicleOpt.get();
                Map<String, Object> car = new HashMap<>();
                car.put("id", vehicle.getVehicleId());
                car.put("model", vehicle.getVehicleModel());
                car.put("type", vehicle.getVehicleType());
                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());
                car.put("dailyRate", vehicle.getPricePerDay());
                car.put("hourlyRate", vehicle.getPricePerHour());
                car.put("monthlyRate", vehicle.getPricePerMonth());
                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());
                car.put("status", vehicle.getStatus());
                response.put("status", "success");
                response.put("data", car);
            } else {
                response.put("status", "error");
                response.put("message", "Vehicle not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get vehicle: " + e.getMessage());
        }
        
        return response;
    }
}