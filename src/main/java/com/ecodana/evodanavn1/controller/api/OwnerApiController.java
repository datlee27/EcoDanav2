package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.service.VehicleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/owner/api")
public class OwnerApiController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/cars/{id}")
    public ResponseEntity<?> getCarForEdit(@PathVariable String id) {
        try {
            return vehicleService.getVehicleById(id).map(vehicle -> {
                Map<String, Object> car = new HashMap<>();
                car.put("vehicleId", vehicle.getVehicleId());
                car.put("vehicleModel", vehicle.getVehicleModel());
                car.put("type", vehicle.getVehicleType().name());
                if (vehicle.getTransmissionType() != null) {
                    car.put("transmission", Map.of("transmissionTypeId", vehicle.getTransmissionType().getTransmissionTypeId()));
                }
                if (vehicle.getCategory() != null) {
                    car.put("category", Map.of("categoryId", vehicle.getCategory().getCategoryId()));
                }
                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());
                try {
                    if (vehicle.getRentalPrices() != null && !vehicle.getRentalPrices().isEmpty()) {
                        Map<String, Object> prices = new ObjectMapper().readValue(vehicle.getRentalPrices(), new TypeReference<Map<String, Object>>() {});
                        car.put("hourlyRate", prices.get("hourly"));
                        car.put("dailyRate", prices.get("daily"));
                        car.put("monthlyRate", prices.get("monthly"));
                    } else {
                        car.put("hourlyRate", 0);
                        car.put("dailyRate", 0);
                        car.put("monthlyRate", 0);
                    }
                } catch (Exception e) {
                    car.put("hourlyRate", 0);
                    car.put("dailyRate", 0);
                    car.put("monthlyRate", 0);
                }
                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());
                car.put("status", vehicle.getStatus().name());
                car.put("yearManufactured", vehicle.getYearManufactured());
                return ResponseEntity.ok(car);
            }).orElse(ResponseEntity.status(404).body(Map.of("message", "Vehicle not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to get vehicle: " + e.getMessage()));
        }

    }
}