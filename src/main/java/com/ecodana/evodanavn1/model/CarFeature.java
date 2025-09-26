package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CarFeature")
public class CarFeature {
    @Id
    @Column(name = "FeatureId", length = 36)
    private String featureId;
    
    @Column(name = "FeatureName", length = 100, nullable = false)
    private String featureName;
    
    // Constructors
    public CarFeature() {}
    
    public CarFeature(String featureId, String featureName) {
        this.featureId = featureId;
        this.featureName = featureName;
    }
    
    // Getters/Setters
    public String getFeatureId() { return featureId; }
    public void setFeatureId(String featureId) { this.featureId = featureId; }
    
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
}