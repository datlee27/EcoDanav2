package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.model.Vehicle; // Thêm import này
import com.ecodana.evodanavn1.service.VehicleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList; // Thêm import này
import java.util.HashMap;
import java.util.List; // Thêm import này
import java.util.Map;

@RestController
@RequestMapping("/owner/api")
public class OwnerApiController {

    @Autowired
    private VehicleService vehicleService;

    // Thêm ObjectMapper để parse JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/cars/{id}")
    public ResponseEntity<?> getCarForEdit(@PathVariable String id) {
        try {
            // Sử dụng getVehicleById để lấy đối tượng Vehicle đầy đủ
            return vehicleService.getVehicleById(id).map(vehicle -> {
                Map<String, Object> car = new HashMap<>();
                car.put("vehicleId", vehicle.getVehicleId());
                car.put("vehicleModel", vehicle.getVehicleModel());

                if (vehicle.getVehicleType() != null) {
                    car.put("type", vehicle.getVehicleType().name());
                }

                // --- SỬA ĐỔI: Tạo object lồng nhau cho JS ---
                if (vehicle.getTransmissionType() != null) {
                    car.put("transmission", Map.of("transmissionTypeId", vehicle.getTransmissionType().getTransmissionTypeId()));
                } else {
                    car.put("transmission", null); // Gửi null nếu không có
                }

                // --- SỬA ĐỔI: Tạo object lồng nhau cho JS ---
                if (vehicle.getCategory() != null) {
                    car.put("category", Map.of("categoryId", vehicle.getCategory().getCategoryId()));
                } else {
                    car.put("category", null); // Gửi null nếu không có
                }

                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());

                // --- SỬA ĐỔI: Sử dụng hàm get...FromJson() ---
                try {
                    // Sử dụng các hàm transient của Vehicle.java để lấy giá đã parse
                    car.put("hourlyRate", vehicle.getHourlyPriceFromJson());
                    car.put("dailyRate", vehicle.getDailyPriceFromJson());
                    car.put("monthlyRate", vehicle.getMonthlyPriceFromJson());
                } catch (Exception e) {
                    car.put("hourlyRate", 0);
                    car.put("dailyRate", 0);
                    car.put("monthlyRate", 0);
                }

                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());

                if(vehicle.getStatus() != null) {
                    car.put("status", vehicle.getStatus().name());
                }

                car.put("yearManufactured", vehicle.getYearManufactured());

                // --- BỔ SUNG CÁC TRƯỜNG CÒN THIẾU ---

                // 1. Thêm Ảnh chính
                car.put("mainImageUrl", vehicle.getMainImageUrl());

                // 2. Thêm Ảnh phụ (dùng hàm transient đã parse sẵn)
                car.put("imageUrls", vehicle.getImageUrlsFromJson());

                // 3. Thêm Tính năng (dùng hàm transient đã parse sẵn)
                car.put("features", vehicle.getFeaturesFromJson());

                // --- KẾT THÚC BỔ SUNG ---

                return ResponseEntity.ok(car);
            }).orElse(ResponseEntity.status(404).body(Map.of("message", "Vehicle not found")));
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console server để debug
            return ResponseEntity.status(500).body(Map.of("message", "Failed to get vehicle: " + e.getMessage()));
        }
    }
}