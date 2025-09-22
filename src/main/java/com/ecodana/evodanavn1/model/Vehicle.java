package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Vehicle")
public class Vehicle {
    @Id
    @Column(name = "VehicleId", length = 36)
    private String vehicleId;
    
    @Column(name = "BrandId", length = 36, nullable = false)
    private String brandId;
    
    @Column(name = "VehicleModel", length = 50, nullable = false)
    private String vehicleModel;
    
    @Column(name = "YearManufactured")
    private Integer yearManufactured;
    
    @Column(name = "TransmissionTypeId", length = 36)
    private String transmissionTypeId;
    
    @Column(name = "FuelTypeId", length = 36, nullable = false)
    private String fuelTypeId;
    
    @Column(name = "LicensePlate", length = 20, nullable = false)
    private String licensePlate;
    
    @Column(name = "Seats", nullable = false)
    private Integer seats;
    
    @Column(name = "Odometer", nullable = false)
    private Integer odometer;
    
    @Column(name = "PricePerHour", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerHour;
    
    @Column(name = "PricePerDay", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerDay;
    
    @Column(name = "PricePerMonth", precision = 10, scale = 2, nullable = false)
    private BigDecimal pricePerMonth;
    
    @Column(name = "Status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "Description", length = 500)
    private String description;
    
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "CategoryId", length = 36)
    private String categoryId;
    
    @Column(name = "LastUpdatedBy", length = 36)
    private String lastUpdatedBy;
    
    @Column(name = "VehicleType", length = 20, nullable = false)
    private String vehicleType;
    
    @Column(name = "RequiresLicense", nullable = false)
    private Boolean requiresLicense = true;
    
    @Column(name = "BatteryCapacity", precision = 10, scale = 2)
    private BigDecimal batteryCapacity;
    
    // Constructors
    public Vehicle() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters/Setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    
    public Integer getYearManufactured() { return yearManufactured; }
    public void setYearManufactured(Integer yearManufactured) { this.yearManufactured = yearManufactured; }
    
    public String getTransmissionTypeId() { return transmissionTypeId; }
    public void setTransmissionTypeId(String transmissionTypeId) { this.transmissionTypeId = transmissionTypeId; }
    
    public String getFuelTypeId() { return fuelTypeId; }
    public void setFuelTypeId(String fuelTypeId) { this.fuelTypeId = fuelTypeId; }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    
    public Integer getOdometer() { return odometer; }
    public void setOdometer(Integer odometer) { this.odometer = odometer; }
    
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }
    
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    
    public BigDecimal getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public Boolean getRequiresLicense() { return requiresLicense; }
    public void setRequiresLicense(Boolean requiresLicense) { this.requiresLicense = requiresLicense; }
    
    public BigDecimal getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(BigDecimal batteryCapacity) { this.batteryCapacity = batteryCapacity; }
}