package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Insurance")
public class Insurance {
    @Id
    @Column(name = "InsuranceId", length = 36)
    private String insuranceId;
    
    @Column(name = "InsuranceName", length = 100, nullable = false)
    private String insuranceName;
    
    @Column(name = "InsuranceType", length = 50, nullable = false)
    private String insuranceType;
    
    @Column(name = "BaseRatePerDay", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseRatePerDay;
    
    @Column(name = "PercentageRate", precision = 5, scale = 2)
    private BigDecimal percentageRate;
    
    @Column(name = "CoverageAmount", precision = 15, scale = 2, nullable = false)
    private BigDecimal coverageAmount;
    
    @Column(name = "ApplicableVehicleSeats", length = 50)
    private String applicableVehicleSeats;
    
    @Column(name = "Description", length = 500)
    private String description;
    
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    
    // Constructors
    public Insurance() {
        this.createdDate = LocalDateTime.now();
        this.isActive = true;
    }
    
    // Getters/Setters
    public String getInsuranceId() { return insuranceId; }
    public void setInsuranceId(String insuranceId) { this.insuranceId = insuranceId; }
    
    public String getInsuranceName() { return insuranceName; }
    public void setInsuranceName(String insuranceName) { this.insuranceName = insuranceName; }
    
    public String getInsuranceType() { return insuranceType; }
    public void setInsuranceType(String insuranceType) { this.insuranceType = insuranceType; }
    
    public BigDecimal getBaseRatePerDay() { return baseRatePerDay; }
    public void setBaseRatePerDay(BigDecimal baseRatePerDay) { this.baseRatePerDay = baseRatePerDay; }
    
    public BigDecimal getPercentageRate() { return percentageRate; }
    public void setPercentageRate(BigDecimal percentageRate) { this.percentageRate = percentageRate; }
    
    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(BigDecimal coverageAmount) { this.coverageAmount = coverageAmount; }
    
    public String getApplicableVehicleSeats() { return applicableVehicleSeats; }
    public void setApplicableVehicleSeats(String applicableVehicleSeats) { this.applicableVehicleSeats = applicableVehicleSeats; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
