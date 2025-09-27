package com.ecodana.evodanavn1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.Insurance;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, String> {
    
    /**
     * Find active insurances
     * @return list of active insurances
     */
    @Query("SELECT i FROM Insurance i WHERE i.isActive = true")
    List<Insurance> findActiveInsurances();
    
    /**
     * Find insurances by type
     * @param insuranceType the insurance type
     * @return list of insurances with the type
     */
    List<Insurance> findByInsuranceType(String insuranceType);
    
    /**
     * Find insurances by applicable vehicle seats
     * @param applicableVehicleSeats the applicable vehicle seats
     * @return list of insurances for the vehicle seats
     */
    List<Insurance> findByApplicableVehicleSeats(String applicableVehicleSeats);
    
    /**
     * Find insurances by coverage amount range
     * @param minCoverage minimum coverage amount
     * @param maxCoverage maximum coverage amount
     * @return list of insurances in coverage range
     */
    @Query("SELECT i FROM Insurance i WHERE i.coverageAmount BETWEEN :minCoverage AND :maxCoverage AND i.isActive = true")
    List<Insurance> findByCoverageRange(@Param("minCoverage") java.math.BigDecimal minCoverage, 
                                        @Param("maxCoverage") java.math.BigDecimal maxCoverage);
}
