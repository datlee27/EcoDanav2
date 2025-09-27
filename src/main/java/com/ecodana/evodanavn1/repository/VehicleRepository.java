package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    
    /**
     * Find vehicles by status
     * @param status the vehicle status
     * @return list of vehicles with the status
     */
    List<Vehicle> findByStatus(String status);
    
    /**
     * Find vehicles by vehicle type
     * @param vehicleType the vehicle type
     * @return list of vehicles of the type
     */
    List<Vehicle> findByVehicleType(String vehicleType);
    
    /**
     * Find available vehicles
     * @return list of available vehicles
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'Available'")
    List<Vehicle> findAvailableVehicles();
    
    /**
     * Find vehicles by brand ID
     * @param brandId the brand ID
     * @return list of vehicles from the brand
     */
    List<Vehicle> findByBrandId(String brandId);
    
    /**
     * Find vehicles by category ID
     * @param categoryId the category ID
     * @return list of vehicles in the category
     */
    List<Vehicle> findByCategoryId(String categoryId);
    
    /**
     * Find vehicles by fuel type ID
     * @param fuelTypeId the fuel type ID
     * @return list of vehicles with the fuel type
     */
    List<Vehicle> findByFuelTypeId(String fuelTypeId);
    
    /**
     * Find vehicles by transmission type ID
     * @param transmissionTypeId the transmission type ID
     * @return list of vehicles with the transmission type
     */
    List<Vehicle> findByTransmissionTypeId(String transmissionTypeId);
    
    /**
     * Find vehicles by license plate
     * @param licensePlate the license plate
     * @return optional vehicle
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Check if vehicle exists by license plate
     * @param licensePlate the license plate
     * @return true if exists, false otherwise
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Find vehicles by price range
     * @param minPrice minimum price per day
     * @param maxPrice maximum price per day
     * @return list of vehicles in price range
     */
    @Query("SELECT v FROM Vehicle v WHERE v.pricePerDay BETWEEN :minPrice AND :maxPrice")
    List<Vehicle> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                   @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Find vehicles by seats
     * @param seats the number of seats
     * @return list of vehicles with the number of seats
     */
    List<Vehicle> findBySeats(Integer seats);
    
    /**
     * Find vehicles that require license
     * @param requiresLicense whether license is required
     * @return list of vehicles based on license requirement
     */
    List<Vehicle> findByRequiresLicense(Boolean requiresLicense);
}
