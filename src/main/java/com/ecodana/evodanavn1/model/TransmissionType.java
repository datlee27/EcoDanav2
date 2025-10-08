package com.ecodana.evodanavn1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TransmissionTypes")
public class TransmissionType {
    @Id
    @Column(name = "TransmissionTypeId")
    private Integer transmissionTypeId;

    @Column(name = "TransmissionTypeName", length = 100, nullable = false)
    private String transmissionTypeName;

    // Getters and Setters
    public String getTransmissionTypeId() {
        return transmissionTypeId.toString();
    }

    public void setTransmissionTypeId(Integer transmissionTypeId) {
        this.transmissionTypeId = transmissionTypeId;
    }

    public String getTransmissionTypeName() {
        return transmissionTypeName;
    }

    public void setTransmissionTypeName(String transmissionTypeName) {
        this.transmissionTypeName = transmissionTypeName;
    }
}
