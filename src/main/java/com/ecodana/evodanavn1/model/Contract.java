package com.ecodana.evodanavn1.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Contract")
public class Contract {
    @Id
    @Column(name = "ContractId", length = 36)
    private String contractId;
    
    @Column(name = "ContractCode", length = 30, nullable = false, unique = true)
    private String contractCode;
    
    @Column(name = "UserId", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "BookingId", length = 36, nullable = false)
    private String bookingId;
    
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "SignedDate")
    private LocalDateTime signedDate;
    
    @Column(name = "CompletedDate")
    private LocalDateTime completedDate;
    
    @Column(name = "Status", length = 20, nullable = false)
    private String status = "Created";
    
    @Column(name = "TermsAccepted", nullable = false)
    private Boolean termsAccepted = false;
    
    @Column(name = "SignatureData", columnDefinition = "TEXT")
    private String signatureData;
    
    @Column(name = "SignatureMethod", length = 20)
    private String signatureMethod;
    
    @Column(name = "ContractPdfUrl", length = 500)
    private String contractPdfUrl;
    
    @Column(name = "ContractFileType", length = 20)
    private String contractFileType;
    
    @Column(name = "Notes", length = 500)
    private String notes;
    
    @Column(name = "CancellationReason", length = 500)
    private String cancellationReason;
    
    // Constructors
    public Contract() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters/Setters
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    
    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getSignedDate() { return signedDate; }
    public void setSignedDate(LocalDateTime signedDate) { this.signedDate = signedDate; }
    
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Boolean getTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(Boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    
    public String getSignatureData() { return signatureData; }
    public void setSignatureData(String signatureData) { this.signatureData = signatureData; }
    
    public String getSignatureMethod() { return signatureMethod; }
    public void setSignatureMethod(String signatureMethod) { this.signatureMethod = signatureMethod; }
    
    public String getContractPdfUrl() { return contractPdfUrl; }
    public void setContractPdfUrl(String contractPdfUrl) { this.contractPdfUrl = contractPdfUrl; }
    
    public String getContractFileType() { return contractFileType; }
    public void setContractFileType(String contractFileType) { this.contractFileType = contractFileType; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
}
