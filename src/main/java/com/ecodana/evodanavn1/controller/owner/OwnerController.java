package com.ecodana.evodanavn1.controller.owner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        // Mock data for owner
        Map<String, Object> owner = new HashMap<>();
        owner.put("firstName", "John");
        owner.put("name", "John Smith");
        owner.put("avatar", "/images/john-smith.jpg");
        owner.put("email", principal != null ? principal.getName() : "john@example.com");

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

    @PostMapping("/cars")
    public String addCar(@RequestParam Map<String, String> carData,
                         @RequestParam("images") MultipartFile[] images) {
        // Simulate adding a car (no actual storage)
        return "redirect:/owner/dashboard?section=cars&success=car-added";
    }

    @PutMapping("/cars/{id}")
    public String updateCar(@PathVariable Long id,
                            @RequestParam Map<String, String> carData,
                            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        // Simulate updating a car
        return "redirect:/owner/dashboard?section=cars&success=car-updated";
    }

    @DeleteMapping("/cars/{id}")
    @ResponseBody
    public Map<String, Object> deleteCar(@PathVariable Long id) {
        // Simulate deleting a car
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

    @PostMapping("/bookings/{id}/accept")
    @ResponseBody
    public Map<String, Object> acceptBooking(@PathVariable Long id) {
        // Simulate accepting a booking
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

    @PostMapping("/bookings/{id}/decline")
    @ResponseBody
    public Map<String, Object> declineBooking(@PathVariable Long id) {
        // Simulate declining a booking
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam Map<String, String> ownerUpdate) {
        // Simulate updating profile
        return "redirect:/owner/dashboard?section=profile&success=profile-updated";
    }

    @GetMapping("/cars/{id}/edit")
    @ResponseBody
    public Map<String, Object> getCarForEdit(@PathVariable Long id) {
        // Mock car data for editing
        Map<String, Object> car = new HashMap<>();
        car.put("id", id);
        car.put("brand", "BMW");
        car.put("model", "X5");
        car.put("rating", 4.9);
        car.put("seats", 5);
        car.put("transmission", "Automatic");
        car.put("fuelType", "Petrol");
        car.put("year", 2023);
        car.put("dailyRate", 320);
        return car;
    }
}