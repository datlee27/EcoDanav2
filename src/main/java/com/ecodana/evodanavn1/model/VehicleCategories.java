package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "VehicleCategories")
public class VehicleCategories {
    @Id
    @Column(name = "CategoryId", length = 36)
    private String categoryId;
    
    @Column(name = "CategoryName", length = 100, nullable = false)
    private String categoryName;
    
    @Column(name = "VehicleType", length = 20, nullable = false)
    private String vehicleType;
    
    // Constructors
    public VehicleCategories() {}
    
    public VehicleCategories(String categoryId, String categoryName, String vehicleType) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.vehicleType = vehicleType;
    }
    
    // Getters/Setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
}