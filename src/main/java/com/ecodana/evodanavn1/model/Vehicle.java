package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.*;

@Entity
@Table(name = "Vehicle")
public class Vehicle {
    @Id
    @Column(name = "VehicleId", length = 36)
    private String vehicleId;

    @Column(name = "BrandId", length = 36)
    private String brandId;

    @Column(name = "VehicleModel", length = 50, nullable = false)
    private String vehicleModel;

    @Column(name = "YearManufactured")
    private Integer yearManufactured;
    @Column(name = "FuelTypeId", length = 36)
    private String fuelTypeId;

    @Column(name = "LicensePlate", length = 20, nullable = false)
    private String licensePlate;

    @Column(name = "Seats")
    private Integer seats;

    @Column(name = "Odometer")
    private Integer odometer;

    @Column(name = "PricePerHour", precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "PricePerDay", precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(name = "PricePerMonth", precision = 10, scale = 2)
    private BigDecimal pricePerMonth;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;
    @Column(name = "RentalPrices", columnDefinition = "JSON")
    private String rentalPrices;

    @Column(name = "LastUpdatedBy", length = 36)
    private String lastUpdatedBy;

    @Column(name = "VehicleType", length = 20)
    private String vehicleType;

    @Column(name = "RequiresLicense")
    private Boolean requiresLicense = true;

    @Column(name = "BatteryCapacity", precision = 10, scale = 2)
    private BigDecimal batteryCapacity;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId", referencedColumnName = "CategoryId")
    private VehicleCategories category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TransmissionTypeId", referencedColumnName = "TransmissionTypeId")
    private TransmissionType transmissionType;

    @Column(name = "MainImageUrl", length = 255)
    private String mainImageUrl;

    @Column(name = "ImageUrls", columnDefinition = "JSON")
    private String imageUrls; // JSON Array: ["url1", "url2"]

    @Column(name = "Features", columnDefinition = "JSON")
    private String features; // JSON Array: ["GPS", "Camera 360"]

    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Constructors
    public Vehicle() {
        this.createdDate = LocalDateTime.now();
        this.status = "Available";
    }

    @Transient
    public BigDecimal getDailyPriceFromJson() {
        if (this.rentalPrices == null || this.rentalPrices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            Map<String, Object> prices = objectMapper.readValue(this.rentalPrices, new TypeReference<Map<String, Object>>() {});
            Object dailyPrice = prices.get("daily");
            if (dailyPrice instanceof Number) {
                return new BigDecimal(dailyPrice.toString());
            }
        } catch (Exception e) {
            System.err.println("Error parsing rentalPrices JSON for vehicle " + this.vehicleId + ": " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    public String getFuelTypeId() { return fuelTypeId; }
    public void setFuelTypeId(String fuelTypeId) { this.fuelTypeId = fuelTypeId; }
    public Integer getTransmissionTypeId() { return (transmissionType != null) ? transmissionType.getTransmissionTypeId() : null; }
    public void setTransmissionTypeId(String transmissionTypeId) {
        if (this.transmissionType == null) {
            this.transmissionType = new TransmissionType();
        }
        this.transmissionType.setTransmissionTypeId(Integer.parseInt(transmissionTypeId));
    }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public BigDecimal getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(BigDecimal pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public Integer getYearManufactured() { return yearManufactured; }
    public void setYearManufactured(Integer yearManufactured) { this.yearManufactured = yearManufactured; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public Integer getOdometer() { return odometer; }
    public void setOdometer(Integer odometer) { this.odometer = odometer; }
    public String getRentalPrices() { return rentalPrices; }
    public void setRentalPrices(String rentalPrices) { this.rentalPrices = rentalPrices; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getCategoryId() { return (category != null) ? category.getCategoryId() : null; }
    public void setCategoryId(String categoryId) {
        if (this.category == null) {
            this.category = new VehicleCategories();
        }
        this.category.setCategoryId(Integer.parseInt(categoryId));
    }

    public String getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public Boolean getRequiresLicense() { return requiresLicense; }
    public void setRequiresLicense(Boolean requiresLicense) { this.requiresLicense = requiresLicense; }
    public BigDecimal getBatteryCapacity() { return batteryCapacity; }
    public void setBatteryCapacity(BigDecimal batteryCapacity) { this.batteryCapacity = batteryCapacity; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public VehicleCategories getCategory() {
        return category;
    }

    public void setCategory(VehicleCategories category) {
        this.category = category;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }
}