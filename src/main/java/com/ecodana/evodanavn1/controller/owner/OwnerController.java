package com.ecodana.evodanavn1.controller.owner;

import java.math.BigDecimal;
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
import com.ecodana.evodanavn1.model.Vehicle;
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

    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        if (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser) && !userService.isOwner(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Owner/Staff/Admin role required.");
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        User currentUser = (User) session.getAttribute("currentUser");
        User userWithRole = userService.getUserWithRole(currentUser.getEmail());
        if (userWithRole == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        try {
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
            // ... (Phần mock data giữ nguyên để dự phòng)
            return "owner/dashboard";
        }
    }

    @PostMapping("/cars")
    public String addCar(@RequestParam Map<String, String> carData,
                         @RequestParam(value = "images", required = false) MultipartFile[] images,
                         HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleId(java.util.UUID.randomUUID().toString());
            vehicle.setVehicleModel(carData.get("model"));
            vehicle.setVehicleType(carData.get("type"));
            vehicle.setLicensePlate(carData.get("licensePlate"));
            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

            String hourlyRate = carData.getOrDefault("hourlyRate", "0").replaceAll("[^\\d.]", "");
            String dailyRate = carData.getOrDefault("dailyRate", "0").replaceAll("[^\\d.]", "");
            String monthlyRate = carData.getOrDefault("monthlyRate", "0").replaceAll("[^\\d.]", "");

            String rentalPricesJson = String.format(
                    "{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}",
                    hourlyRate, dailyRate, monthlyRate
            );
            vehicle.setRentalPrices(rentalPricesJson);

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

                String hourlyRate = carData.getOrDefault("hourlyRate", "0").replaceAll("[^\\d.]", "");
                String dailyRate = carData.getOrDefault("dailyRate", "0").replaceAll("[^\\d.]", "");
                String monthlyRate = carData.getOrDefault("monthlyRate", "0").replaceAll("[^\\d.]", "");

                String rentalPricesJson = String.format(
                        "{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}",
                        hourlyRate, dailyRate, monthlyRate
                );
                vehicle.setRentalPrices(rentalPricesJson);

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

    // Các phương thức khác giữ nguyên...
    // deleteCar, acceptBooking, declineBooking, updateProfile
}