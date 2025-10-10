package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.repository.VehicleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        return vehicleRepository.findAvailableVehicles().stream().limit(2).collect(Collectors.toList());
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
        stats.put("totalVehicles", allVehicles.size());
        stats.put("availableVehicles", getAvailableVehicles().size());
        stats.put("inUseVehicles", allVehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.Rented).count());
        stats.put("maintenanceVehicles", allVehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.Maintenance).count());
        return stats;
    }

    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status);
    }

    public Vehicle updateVehicleStatus(String vehicleId, String status) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.setStatus(Vehicle.VehicleStatus.valueOf(status));
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
        return str == null || str.trim().isEmpty();
    }

    public List<Vehicle> filterVehicles(String location, String pickupDate, String returnDate, String pickupTime, String returnTime, String category, String vehicleType, String budget, Integer seats, Boolean requiresLicense) {
        Stream<Vehicle> vehicleStream = getAllVehicles().stream();

        // TODO: Implement location-based filtering.
        if (!isNullOrEmpty(pickupDate) || !isNullOrEmpty(returnDate) || !isNullOrEmpty(pickupTime) || !isNullOrEmpty(returnTime)) {
            vehicleStream = vehicleStream.filter(vehicle -> vehicle.getStatus() == Vehicle.VehicleStatus.Available);
        }

        if (!isNullOrEmpty(vehicleType)) {
            vehicleStream = vehicleStream.filter(vehicle -> vehicleType.equals(vehicle.getVehicleType().name()));
        }

        if (!isNullOrEmpty(category)) {
            vehicleStream = vehicleStream.filter(vehicle -> vehicle.getCategory() != null && category.equalsIgnoreCase(vehicle.getCategory().getCategoryName()));
        }

        if (!isNullOrEmpty(budget)) {
            vehicleStream = vehicleStream.filter(vehicle -> {
                BigDecimal dailyPrice = vehicle.getDailyPriceFromJson();
                if ("under500k".equals(budget)) {
                    return dailyPrice.compareTo(new BigDecimal("500000")) < 0;
                } else if ("over500k".equals(budget)) {
                    return dailyPrice.compareTo(new BigDecimal("500000")) >= 0;
                }
                return true;
            });
        }

        if (seats != null && seats > 0) {
            vehicleStream = vehicleStream.filter(vehicle -> seats.equals(vehicle.getSeats()));
        }

        if (requiresLicense != null) {
            vehicleStream = vehicleStream.filter(vehicle -> requiresLicense.equals(vehicle.getRequiresLicense()));
        }

        return vehicleStream.collect(Collectors.toList());
    }
}