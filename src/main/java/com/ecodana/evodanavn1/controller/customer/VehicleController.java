package com.ecodana.evodanavn1.controller.customer;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VehicleController {

    private final VehicleService vehicleService;

    @Autowired
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/vehicles")
    public String listVehicles(@RequestParam(required = false) String location,
                               @RequestParam(required = false) String pickupDate,
                               @RequestParam(required = false) String returnDate,
                               @RequestParam(required = false) String pickupTime,
                               @RequestParam(required = false) String returnTime,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String vehicleType,
                               @RequestParam(required = false) String budget,
                               @RequestParam(required = false) Integer seats,
                               @RequestParam(required = false) Boolean requiresLicense,
                               Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        List<Vehicle> vehicles = vehicleService.filterVehicles(location, pickupDate, returnDate, pickupTime, returnTime, category, vehicleType, budget, seats, requiresLicense);
        model.addAttribute("vehicles", vehicles);

        // Add all filter parameters to the model to be used in the view
        model.addAttribute("selectedLocation", location);
        model.addAttribute("selectedPickupDate", pickupDate);
        model.addAttribute("selectedReturnDate", returnDate);
        model.addAttribute("selectedPickupTime", pickupTime);
        model.addAttribute("selectedReturnTime", returnTime);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedVehicleType", vehicleType);
        model.addAttribute("selectedBudget", budget);
        model.addAttribute("selectedSeats", seats);
        model.addAttribute("selectedRequiresLicense", requiresLicense);

        return "customer/vehicles";
    }

    @GetMapping("/vehicles/{id}")
    public String vehicleDetail(@PathVariable("id") String vehicleId, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        model.addAttribute("vehicle", vehicle);

        // Get related vehicles (same type, different model)
        List<Vehicle> relatedVehicles = vehicleService.getVehiclesByType(vehicle.getVehicleType())
                .stream()
                .filter(v -> !v.getVehicleId().equals(vehicleId) && "Available".equals(v.getStatus()))
                .limit(3)
                .toList();
        model.addAttribute("relatedVehicles", relatedVehicles);

        return "customer/vehicle-detail";
    }
}
