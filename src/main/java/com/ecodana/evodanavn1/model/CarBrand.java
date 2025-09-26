package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CarBrand")
public class CarBrand {
    @Id
    @Column(name = "BrandId", length = 36)
    private String brandId;
    
    @Column(name = "BrandName", length = 100, nullable = false)
    private String brandName;
    
    // Constructors
    public CarBrand() {}
    
    public CarBrand(String brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }
    
    // Getters/Setters
    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
}