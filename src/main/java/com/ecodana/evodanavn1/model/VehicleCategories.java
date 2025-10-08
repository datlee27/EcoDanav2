package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "VehicleCategories")
public class VehicleCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoryId")
    private Integer categoryId;

    @Column(name = "CategoryName", length = 100, nullable = false, unique = true)
    private String categoryName;
    @Column(name = "VehicleType", length = 20, nullable = false)
    private String vehicleType;
    // Constructors
    public VehicleCategories() {}

    public VehicleCategories(String categoryName) {
        this.categoryName = categoryName;
    }

    // Getters/Setters
    public String getCategoryId() { return categoryId.toString(); }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
}