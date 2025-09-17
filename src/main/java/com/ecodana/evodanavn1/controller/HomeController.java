package com.ecodana.evodanavn1.controller;

import com.ecodana.evodanavn1.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredVehicles", vehicleService.getAllVehicles().subList(0, 3));  // Featured 3 vehicles
        return "home";
    }

    @GetMapping("/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.getAllVehicles());
        return "vehicles";
    }

    @GetMapping("/vehicle-detail")
    public String vehicleDetail(String id, Model model) {
        model.addAttribute("vehicle", vehicleService.getVehicleById(id));
        return "vehicle-detail";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation(Model model) {
        // Mock summary
        model.addAttribute("bookingSummary", "Tesla Model 3 - Dec 15-18, 2024 - $267");
        return "booking-confirmation";
    }
}