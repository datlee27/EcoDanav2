package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "VehicleCategories")
public class VehicleCategory {
    @Id
    @Column(name = "CategoryId")
    private Integer categoryId;

    @Column(name = "CategoryName", length = 100, nullable = false)
    private String categoryName;

    // Getters and Setters
    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
