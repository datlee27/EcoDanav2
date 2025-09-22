package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "FuelType")
public class FuelType {
    @Id
    @Column(name = "FuelTypeId", length = 36)
    private String fuelTypeId;
    
    @Column(name = "FuelName", length = 100, nullable = false)
    private String fuelName;
    
    // Constructors
    public FuelType() {}
    
    public FuelType(String fuelTypeId, String fuelName) {
        this.fuelTypeId = fuelTypeId;
        this.fuelName = fuelName;
    }
    
    // Getters/Setters
    public String getFuelTypeId() { return fuelTypeId; }
    public void setFuelTypeId(String fuelTypeId) { this.fuelTypeId = fuelTypeId; }
    
    public String getFuelName() { return fuelName; }
    public void setFuelName(String fuelName) { this.fuelName = fuelName; }
}
