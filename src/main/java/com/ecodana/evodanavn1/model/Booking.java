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

    @Column(name = "DepositAmountRequired", precision = 10, scale = 2, nullable = false)
    private BigDecimal depositAmountRequired = BigDecimal.ZERO;

    @Column(name = "RemainingAmount", precision = 10, scale = 2, nullable = false)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

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

    @Transient
    private boolean hasFeedback = false;

    @Transient
    private boolean canReview = false;

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
    public BigDecimal getDepositAmountRequired() { return depositAmountRequired; }
    public void setDepositAmountRequired(BigDecimal depositAmountRequired) { this.depositAmountRequired = depositAmountRequired; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }
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
    public boolean isHasFeedback() { return hasFeedback; }
    public void setHasFeedback(boolean hasFeedback) { this.hasFeedback = hasFeedback; }
    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean canReview) { this.canReview = canReview; }

    public enum BookingStatus {
        Pending,          // Khách vừa tạo, chờ chủ xe duyệt
        Approved,         // Chủ xe đã duyệt (trạng thái trung gian)
        AwaitingDeposit,  // Đã duyệt, chờ khách thanh toán 20% cọc
        Confirmed,        // Khách đã thanh toán cọc, đơn đã chắc chắn
        Rejected,         // Chủ xe từ chối
        Ongoing,          // Đang trong quá trình thuê (đã nhận xe)
        Completed,        // Đã hoàn tất chuyến đi và thanh toán
        Cancelled         // Đơn bị hủy
    }

    public enum RentalType {
        hourly, daily, monthly
    }
}