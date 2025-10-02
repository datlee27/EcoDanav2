package com.ecodana.evodanavn1.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "UserDocuments")
public class UserDocument {

    @Id
    @Column(name = "DocumentId", length = 36)
    private String documentId;

    @Column(name = "UserId", length = 36, nullable = false)
    private String userId;

    @Column(name = "DocumentType", nullable = false)
    private String documentType; // ENUM('CitizenId', 'DriverLicense', 'Passport')

    @Column(name = "DocumentNumber", length = 50, nullable = false)
    private String documentNumber;

    @Column(name = "FullName", length = 100)
    private String fullName;

    @Column(name = "DOB")
    private LocalDate dob;

    @Column(name = "IssuedDate")
    private LocalDate issuedDate;

    @Column(name = "IssuedPlace", length = 100)
    private String issuedPlace;

    @Column(name = "FrontImageUrl", length = 500)
    private String frontImageUrl;

    @Column(name = "BackImageUrl", length = 500)
    private String backImageUrl;

    @Column(name = "IsVerified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    public UserDocument() {
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }
    public String getIssuedPlace() { return issuedPlace; }
    public void setIssuedPlace(String issuedPlace) { this.issuedPlace = issuedPlace; }
    public String getFrontImageUrl() { return frontImageUrl; }
    public void setFrontImageUrl(String frontImageUrl) { this.frontImageUrl = frontImageUrl; }
    public String getBackImageUrl() { return backImageUrl; }
    public void setBackImageUrl(String backImageUrl) { this.backImageUrl = backImageUrl; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean isVerified) { this.isVerified = isVerified; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}