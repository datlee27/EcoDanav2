package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TransmissionTypes")
public class TransmissionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransmissionTypeId")
    private Integer transmissionTypeId;

    @Column(name = "TransmissionTypeName", length = 100, nullable = false, unique = true)
    private String transmissionTypeName;

    // Constructors
    public TransmissionType() {}

    public TransmissionType(String transmissionTypeName) {
        this.transmissionTypeName = transmissionTypeName;
    }

    // Getters/Setters
    public Integer getTransmissionTypeId() { return transmissionTypeId; }
    public void setTransmissionTypeId(Integer transmissionTypeId) { this.transmissionTypeId = transmissionTypeId; }

    public String getTransmissionTypeName() { return transmissionTypeName; }
    public void setTransmissionTypeName(String transmissionTypeName) { this.transmissionTypeName = transmissionTypeName; }
}