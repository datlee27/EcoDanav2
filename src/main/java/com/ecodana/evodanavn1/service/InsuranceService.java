package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Insurance;
import com.ecodana.evodanavn1.repository.InsuranceRepository;

@Service
public class InsuranceService {
    
    @Autowired
    private InsuranceRepository insuranceRepository;
    
    /**
     * Get all insurances
     * @return list of all insurances
     */
    public List<Insurance> getAllInsurances() {
        return insuranceRepository.findAll();
    }
    
    /**
     * Get active insurances
     * @return list of active insurances
     */
    public List<Insurance> getActiveInsurances() {
        return insuranceRepository.findActiveInsurances();
    }
    
    /**
     * Get insurances by type
     * @param insuranceType the insurance type
     * @return list of insurances with the type
     */
    public List<Insurance> getInsurancesByType(String insuranceType) {
        return insuranceRepository.findByInsuranceType(insuranceType);
    }
    
    /**
     * Get insurances by vehicle seats
     * @param applicableVehicleSeats the applicable vehicle seats
     * @return list of insurances for the vehicle seats
     */
    public List<Insurance> getInsurancesByVehicleSeats(String applicableVehicleSeats) {
        return insuranceRepository.findByApplicableVehicleSeats(applicableVehicleSeats);
    }
    
    /**
     * Get insurances by coverage range
     * @param minCoverage minimum coverage amount
     * @param maxCoverage maximum coverage amount
     * @return list of insurances in coverage range
     */
    public List<Insurance> getInsurancesByCoverageRange(BigDecimal minCoverage, BigDecimal maxCoverage) {
        return insuranceRepository.findByCoverageRange(minCoverage, maxCoverage);
    }
    
    /**
     * Save insurance
     * @param insurance the insurance to save
     * @return saved insurance
     */
    public Insurance saveInsurance(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }
    
    /**
     * Update insurance
     * @param insurance the insurance to update
     * @return updated insurance
     */
    public Insurance updateInsurance(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }
    
    /**
     * Delete insurance
     * @param insuranceId the insurance ID to delete
     */
    public void deleteInsurance(String insuranceId) {
        insuranceRepository.deleteById(insuranceId);
    }
    
    /**
     * Calculate insurance premium
     * @param insurance the insurance
     * @param rentalDays the number of rental days
     * @param vehicleValue the vehicle value
     * @return calculated premium
     */
    public BigDecimal calculatePremium(Insurance insurance, int rentalDays, BigDecimal vehicleValue) {
        if (insurance == null || !insurance.getIsActive()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal basePremium = insurance.getBaseRatePerDay().multiply(new BigDecimal(rentalDays));
        
        if (insurance.getPercentageRate() != null && vehicleValue != null) {
            BigDecimal percentagePremium = vehicleValue.multiply(insurance.getPercentageRate()).divide(new BigDecimal("100"));
            basePremium = basePremium.add(percentagePremium);
        }
        
        return basePremium;
    }
}
