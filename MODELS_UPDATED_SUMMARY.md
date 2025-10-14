# Models Updated - Summary

## ✅ **Đã hoàn thành:**

### **1. Booking.java**
**Thêm 11 fields mới:**
- `approvedBy` (User) - Owner who approved
- `approvedDate` (LocalDateTime)
- `rejectedBy` (User) - Owner who rejected
- `rejectedDate` (LocalDateTime)
- `pickupConfirmedBy` (User) - Staff who confirmed pickup
- `actualPickupDate` (LocalDateTime)
- `returnConfirmedByCustomer` (Boolean)
- `customerReturnDate` (LocalDateTime)
- `returnConfirmedByOwner` (Boolean)
- `ownerConfirmDate` (LocalDateTime)
- `actualReturnDate` (LocalDateTime)

**Updated BookingStatus Enum:**
```java
public enum BookingStatus {
    Pending,                // Legacy - same as PendingPayment
    PendingPayment,         // Waiting for customer payment
    PendingOwnerApproval,   // Payment received, waiting for owner approval
    Approved,               // Owner approved
    Rejected,               // Owner rejected
    Ongoing,                // Currently renting
    Completed,              // Trip completed
    Cancelled               // Cancelled
}
```

---

### **2. Payment.java**
**Thêm 8 fields mới:**
- `heldBy` (User) - Staff holding the money
- `heldDate` (LocalDateTime)
- `transferredToOwner` (Boolean)
- `transferDate` (LocalDateTime)
- `transferredBy` (User)
- `refundDate` (LocalDateTime)
- `refundedBy` (User)
- `refundAmount` (BigDecimal)

**Updated PaymentStatus Enum:**
```java
public enum PaymentStatus {
    Pending,        // Waiting for payment
    Held,           // Staff is holding the money
    Completed,      // Payment completed (transferred to owner)
    Failed,         // Payment failed
    Refunded        // Refunded to customer
}
```

---

## 📝 **Next Steps:**

### **Phase 1: Run Migration**
```bash
run-payment-flow-v2-migration.bat
```

### **Phase 2: Create Endpoints**

#### **StaffController - Add methods:**
1. ✅ `confirmPaymentReceived()` - Xác nhận nhận tiền
2. ✅ `refundPayment()` - Hoàn tiền
3. ✅ `transferToOwner()` - Chuyển tiền cho owner
4. ✅ `confirmPickup()` - Xác nhận giao xe

#### **OwnerController - Create new:**
1. ⏳ `approveBooking()` - Duyệt booking
2. ⏳ `rejectBooking()` - Từ chối booking
3. ⏳ `confirmReturn()` - Xác nhận nhận xe trả

#### **BookingController - Add method:**
1. ⏳ `confirmReturn()` - Customer xác nhận trả xe

---

## 🎯 **Status:**
- ✅ Database migration script created
- ✅ Booking model updated
- ✅ Payment model updated
- ⏳ Endpoints creation (next)
- ⏳ UI updates (after endpoints)

---

## 🚀 **Ready to proceed with:**
1. Run migration script
2. Create Owner Controller
3. Update Staff Controller with new endpoints
4. Test the flow

Bạn có muốn tôi tiếp tục tạo endpoints không?
