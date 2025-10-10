package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Booking")
public class Booking {
    @Id
    @Column(name = "BookingId", length = 36)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleId", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HandledBy")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User handledBy;

    @Column(name = "PickupDateTime", nullable = false)
    private LocalDateTime pickupDateTime;

    @Column(name = "ReturnDateTime", nullable = false)
    private LocalDateTime returnDateTime;

    @Column(name = "TotalAmount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "Status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.Pending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DiscountId")
    private Discount discount;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "CancelReason", length = 500)
    private String cancelReason;

    @Column(name = "BookingCode", length = 20, nullable = false, unique = true)
    private String bookingCode;

    @Column(name = "ExpectedPaymentMethod", length = 50)
    private String expectedPaymentMethod;

    @Column(name = "RentalType", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private RentalType rentalType = RentalType.daily;

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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public User getHandledBy() { return handledBy; }
    public void setHandledBy(User handledBy) { this.handledBy = handledBy; }
    public LocalDateTime getPickupDateTime() { return pickupDateTime; }
    public void setPickupDateTime(LocalDateTime pickupDateTime) { this.pickupDateTime = pickupDateTime; }
    public LocalDateTime getReturnDateTime() { return returnDateTime; }
    public void setReturnDateTime(LocalDateTime returnDateTime) { this.returnDateTime = returnDateTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Discount getDiscount() { return discount; }
    public void setDiscount(Discount discount) { this.discount = discount; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    public String getExpectedPaymentMethod() { return expectedPaymentMethod; }
    public void setExpectedPaymentMethod(String expectedPaymentMethod) { this.expectedPaymentMethod = expectedPaymentMethod; }
    public RentalType getRentalType() { return rentalType; }
    public void setRentalType(RentalType rentalType) { this.rentalType = rentalType; }
    public Boolean getTermsAgreed() { return termsAgreed; }
    public void setTermsAgreed(Boolean termsAgreed) { this.termsAgreed = termsAgreed; }
    public LocalDateTime getTermsAgreedAt() { return termsAgreedAt; }
    public void setTermsAgreedAt(LocalDateTime termsAgreedAt) { this.termsAgreedAt = termsAgreedAt; }
    public String getTermsVersion() { return termsVersion; }
    public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }

    public enum BookingStatus {
        Pending, Approved, Rejected, Ongoing, Completed, Cancelled
    }

    public enum RentalType {
        hourly, daily, monthly
    }
}