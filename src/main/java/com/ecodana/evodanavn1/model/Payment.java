package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Payment")
public class Payment {
    @Id
    @Column(name = "PaymentId", length = 36)
    private String paymentId;
    
    @Column(name = "BookingId", length = 36, nullable = false)
    private String bookingId;
    
    @Column(name = "ContractId", length = 36)
    private String contractId;
    
    @Column(name = "Amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "PaymentMethod", length = 50, nullable = false)
    private String paymentMethod;
    
    @Column(name = "PaymentStatus", length = 20, nullable = false)
    private String paymentStatus = "Pending";
    
    @Column(name = "PaymentType", length = 20, nullable = false)
    private String paymentType = "Deposit";
    
    @Column(name = "TransactionId", length = 100)
    private String transactionId;
    
    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;
    
    @Column(name = "UserId", length = 36)
    private String userId;
    
    @Column(name = "Notes", length = 500)
    private String notes;
    
    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
    
    // Constructors
    public Payment() {
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters/Setters
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = contractId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
