package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.util.List;
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
}