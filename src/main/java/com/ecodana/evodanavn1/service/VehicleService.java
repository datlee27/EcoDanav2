package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.repository.VehicleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles();
    }

    public List<Vehicle> getVehiclesByType(String vehicleType) {
        return vehicleRepository.findByVehicleType(vehicleType);
    }

    public List<Vehicle> getVehiclesByCategory(Integer categoryId) {
        return vehicleRepository.findByCategory_CategoryId(categoryId);
    }

    public List<Vehicle> getVehiclesByTransmissionType(Integer transmissionTypeId) {
        return vehicleRepository.findByTransmissionType_TransmissionTypeId(transmissionTypeId);
    }

    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.findByPriceRange(minPrice, maxPrice);
    }

    public List<Vehicle> getVehiclesBySeats(Integer seats) {
        return vehicleRepository.findBySeats(seats);
    }

    public List<Vehicle> getVehiclesByLicenseRequirement(Boolean requiresLicense) {
        return vehicleRepository.findByRequiresLicense(requiresLicense);
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(String vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }

    public List<Vehicle> getFavoriteVehiclesByUser(User user) {
        return vehicleRepository.findAvailableVehicles().stream().limit(2).toList();
    }

    public BigDecimal getDailyPrice(Vehicle vehicle) {
        try {
            Map<String, Object> prices = objectMapper.readValue(vehicle.getRentalPrices(), new TypeReference<>() {});
            Object dailyPrice = prices.get("daily");
            if (dailyPrice instanceof Number) {
                return new BigDecimal(((Number) dailyPrice).toString());
            }
        } catch (Exception e) {
            // Log error
        }
        return BigDecimal.ZERO;
    }

    public Map<String, Object> getVehicleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Vehicle> allVehicles = getAllVehicles();
        List<Vehicle> availableVehicles = getAvailableVehicles();
        long inUseVehicles = allVehicles.stream().filter(v -> "Rented".equals(v.getStatus())).count();
        long maintenanceVehicles = allVehicles.stream().filter(v -> "Maintenance".equals(v.getStatus())).count();

        stats.put("totalVehicles", allVehicles.size());
        stats.put("availableVehicles", availableVehicles.size());
        stats.put("inUseVehicles", inUseVehicles);
        stats.put("maintenanceVehicles", maintenanceVehicles);

        return stats;
    }

    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status);
    }

    public Vehicle updateVehicleStatus(String vehicleId, String status) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.setStatus(status);
                    return vehicleRepository.save(vehicle);
                })
                .orElse(null);
    }

    public List<Vehicle> searchVehicles(String keyword) {
        return vehicleRepository.searchVehicles(keyword);
    }

    public Map<String, Object> getVehicleAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("statusDistribution", vehicleRepository.findStatusDistribution());
        analytics.put("categoryDistribution", vehicleRepository.findCategoryDistribution());
        return analytics;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public List<Vehicle> filterVehicles(String location, String pickupDate, String returnDate, String pickupTime, String returnTime, String category, String vehicleType, String budget, Integer seats, Boolean requiresLicense) {
        Stream<Vehicle> vehicleStream = getAllVehicles().stream();

        // TODO: Implement location-based filtering. The Vehicle entity currently does not have location data.

        // Filter by availability if date/time is specified
        if (!isNullOrEmpty(pickupDate) || !isNullOrEmpty(returnDate) || !isNullOrEmpty(pickupTime) || !isNullOrEmpty(returnTime)) {
            vehicleStream = vehicleStream.filter(vehicle -> "Available".equalsIgnoreCase(vehicle.getStatus()));
        }

        // Filter by Vehicle Type
        if (!isNullOrEmpty(vehicleType)) {
            vehicleStream = vehicleStream.filter(vehicle -> vehicleType.equals(vehicle.getVehicleType()));
        }

        // Filter by Category
        if (!isNullOrEmpty(category)) {
            vehicleStream = vehicleStream.filter(vehicle -> vehicle.getCategory() != null && category.equalsIgnoreCase(vehicle.getCategory().getCategoryName()));
        }

        // Filter by Budget
        if (!isNullOrEmpty(budget)) {
            vehicleStream = vehicleStream.filter(vehicle -> {
                BigDecimal dailyPrice = getDailyPrice(vehicle);
                if ("under500k".equals(budget)) {
                    return dailyPrice.compareTo(new BigDecimal("500000")) < 0;
                } else if ("over500k".equals(budget)) {
                    return dailyPrice.compareTo(new BigDecimal("500000")) >= 0;
                }
                return true; // No budget filter if value is unknown
            });
        }

        // Filter by Seats
        if (seats != null && seats > 0) {
            vehicleStream = vehicleStream.filter(vehicle -> seats.equals(vehicle.getSeats()));
        }

        // Filter by License Requirement
        if (requiresLicense != null) {
            vehicleStream = vehicleStream.filter(vehicle -> requiresLicense.equals(vehicle.getRequiresLicense()));
        }

        return vehicleStream.collect(Collectors.toList());
    }
}
