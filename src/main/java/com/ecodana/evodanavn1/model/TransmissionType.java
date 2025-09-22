package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TransmissionType")
public class TransmissionType {
    @Id
    @Column(name = "TransmissionTypeId", length = 36)
    private String transmissionTypeId;
    
    @Column(name = "TransmissionName", length = 100, nullable = false)
    private String transmissionName;
    
    // Constructors
    public TransmissionType() {}
    
    public TransmissionType(String transmissionTypeId, String transmissionName) {
        this.transmissionTypeId = transmissionTypeId;
        this.transmissionName = transmissionName;
    }
    
    // Getters/Setters
    public String getTransmissionTypeId() { return transmissionTypeId; }
    public void setTransmissionTypeId(String transmissionTypeId) { this.transmissionTypeId = transmissionTypeId; }
    
    public String getTransmissionName() { return transmissionName; }
    public void setTransmissionName(String transmissionName) { this.transmissionName = transmissionName; }
}
