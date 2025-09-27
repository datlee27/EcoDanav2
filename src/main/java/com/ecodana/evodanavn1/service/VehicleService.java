package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.repository.VehicleRepository;

@Service
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicleRepository.findById(id);
    }
    
    /**
     * Get available vehicles
     * @return list of available vehicles
     */
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles();
    }
    
    /**
     * Get vehicles by type
     * @param vehicleType the vehicle type
     * @return list of vehicles of the type
     */
    public List<Vehicle> getVehiclesByType(String vehicleType) {
        return vehicleRepository.findByVehicleType(vehicleType);
    }
    
    /**
     * Get vehicles by brand
     * @param brandId the brand ID
     * @return list of vehicles from the brand
     */
    public List<Vehicle> getVehiclesByBrand(String brandId) {
        return vehicleRepository.findByBrandId(brandId);
    }
    
    /**
     * Get vehicles by category
     * @param categoryId the category ID
     * @return list of vehicles in the category
     */
    public List<Vehicle> getVehiclesByCategory(String categoryId) {
        return vehicleRepository.findByCategoryId(categoryId);
    }
    
    /**
     * Get vehicles by price range
     * @param minPrice minimum price per day
     * @param maxPrice maximum price per day
     * @return list of vehicles in price range
     */
    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * Get vehicles by seats
     * @param seats the number of seats
     * @return list of vehicles with the number of seats
     */
    public List<Vehicle> getVehiclesBySeats(Integer seats) {
        return vehicleRepository.findBySeats(seats);
    }
    
    /**
     * Get vehicles that require license
     * @param requiresLicense whether license is required
     * @return list of vehicles based on license requirement
     */
    public List<Vehicle> getVehiclesByLicenseRequirement(Boolean requiresLicense) {
        return vehicleRepository.findByRequiresLicense(requiresLicense);
    }
    
    /**
     * Save vehicle
     * @param vehicle the vehicle to save
     * @return saved vehicle
     */
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Update vehicle
     * @param vehicle the vehicle to update
     * @return updated vehicle
     */
    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Delete vehicle
     * @param vehicleId the vehicle ID to delete
     */
    public void deleteVehicle(String vehicleId) {
        vehicleRepository.deleteById(vehicleId);
    }
    
    /**
     * Get favorite vehicles by user (mock implementation)
     * @param user the user
     * @return list of favorite vehicles
     */
    public List<Vehicle> getFavoriteVehiclesByUser(User user) {
        // Mock implementation - return first 2 available vehicles as favorites
        return vehicleRepository.findAvailableVehicles().stream().limit(2).toList();
    }
    
    /**
     * Get vehicle statistics for admin dashboard
     * @return map containing vehicle statistics
     */
    public Map<String, Object> getVehicleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Vehicle> allVehicles = getAllVehicles();
        List<Vehicle> availableVehicles = getAvailableVehicles();
        
        long inUseVehicles = allVehicles.stream().mapToLong(v -> "In Use".equals(v.getStatus()) ? 1 : 0).sum();
        long maintenanceVehicles = allVehicles.stream().mapToLong(v -> "Maintenance".equals(v.getStatus()) ? 1 : 0).sum();
        
        stats.put("totalVehicles", allVehicles.size());
        stats.put("availableVehicles", availableVehicles.size());
        stats.put("inUseVehicles", inUseVehicles);
        stats.put("maintenanceVehicles", maintenanceVehicles);
        
        return stats;
    }
    
    /**
     * Get vehicles by status
     * @param status the vehicle status
     * @return list of vehicles with the status
     */
    public List<Vehicle> getVehiclesByStatus(String status) {
        return vehicleRepository.findByStatus(status);
    }
    
    /**
     * Update vehicle status
     * @param vehicleId the vehicle ID
     * @param status the new status
     * @return updated vehicle or null if not found
     */
    public Vehicle updateVehicleStatus(String vehicleId, String status) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    vehicle.setStatus(status);
                    return vehicleRepository.save(vehicle);
                })
                .orElse(null);
    }
    
    /**
     * Search vehicles by keyword
     * @param keyword the search keyword
     * @return list of matching vehicles
     */
    public List<Vehicle> searchVehicles(String keyword) {
        return vehicleRepository.searchVehicles(keyword);
    }
    
    /**
     * Get vehicle analytics for charts
     * @return map containing chart data
     */
    public Map<String, Object> getVehicleAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Vehicle status distribution
        List<Map<String, Object>> statusDistribution = vehicleRepository.findStatusDistribution();
        analytics.put("statusDistribution", statusDistribution);
        
        // Vehicle category distribution
        List<Map<String, Object>> categoryDistribution = vehicleRepository.findCategoryDistribution();
        analytics.put("categoryDistribution", categoryDistribution);
        
        // Vehicle brand distribution
        List<Map<String, Object>> brandDistribution = vehicleRepository.findBrandDistribution();
        analytics.put("brandDistribution", brandDistribution);
        
        return analytics;
    }
}