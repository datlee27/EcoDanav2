package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Booking")
public class Booking {
    @Id
    @Column(name = "BookingId", length = 36)
    private String bookingId;

    @Column(name = "UserId", length = 36, nullable = false)
    private String userId;

    @Column(name = "VehicleId", length = 36, nullable = false)
    private String vehicleId;

    @Column(name = "HandledBy", length = 36)
    private String handledBy;

    @Column(name = "PickupDateTime", nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(name = "ReturnDateTime", nullable = false)
    private LocalDateTime returnDateTime;

    @Column(name = "TotalAmount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "Status", length = 50, nullable = false)
    private String status = "Pending"; // ENUM('Pending', 'Approved', 'Rejected', 'Ongoing', 'Completed', 'Cancelled')

    @Column(name = "DiscountId", length = 36)
    private String discountId;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "CancelReason", length = 500)
    private String cancelReason;

    @Column(name = "BookingCode", length = 20, nullable = false, unique = true)
    private String bookingCode;

    @Column(name = "ExpectedPaymentMethod", length = 50)
    private String expectedPaymentMethod;

    @Column(name = "RentalType", length = 10, nullable = false)
    private String rentalType = "daily"; // ENUM('hourly', 'daily', 'monthly')

    @Column(name = "TermsAgreed", nullable = false)
    private Boolean termsAgreed = false;

    @Column(name = "TermsAgreedAt")
    private LocalDateTime termsAgreedAt;

    @Column(name = "TermsVersion", length = 10)
    private String termsVersion = "v1.0";

    // Constructors
    public Booking() {
        this.createdDate = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getPickupDateTime() { return pickupDateTime; }
    public void setPickupDateTime(LocalDateTime pickupDateTime) { this.pickupDateTime = pickupDateTime; }
    public LocalDateTime getReturnDateTime() { return returnDateTime; }
    public void setReturnDateTime(LocalDateTime returnDateTime) { this.returnDateTime = returnDateTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiscountId() { return discountId; }
    public void setDiscountId(String discountId) { this.discountId = discountId; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getExpectedPaymentMethod() { return expectedPaymentMethod; }
    public void setExpectedPaymentMethod(String expectedPaymentMethod) { this.expectedPaymentMethod = expectedPaymentMethod; }
    public String getRentalType() { return rentalType; }
    public void setRentalType(String rentalType) { this.rentalType = rentalType; }
    public Boolean getTermsAgreed() { return termsAgreed; }
    public void setTermsAgreed(Boolean termsAgreed) { this.termsAgreed = termsAgreed; }
    public LocalDateTime getTermsAgreedAt() { return termsAgreedAt; }
    public void setTermsAgreedAt(LocalDateTime termsAgreedAt) { this.termsAgreedAt = termsAgreedAt; }
    public String getTermsVersion() { return termsVersion; }
    public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }
}