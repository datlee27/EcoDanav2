# Implementation Guide - Payment Flow V2

## 📋 **Tổng quan**

Hệ thống thanh toán 3 bên: **Customer → Staff → Owner**

---

## 🚀 **Bước 1: Chạy Database Migration**

### **Windows:**
```bash
run-payment-flow-v2-migration.bat
```

### **Manual:**
```bash
mysql -u root -p ecodana < src/main/resources/db/payment-flow-v2-migration.sql
```

### **Verify:**
```sql
DESCRIBE Booking;
DESCRIBE payment;
```

---

## 📝 **Bước 2: Update Java Models**

### **Booking.java - Thêm fields:**

```java
// Approval tracking
private User approvedBy;
private LocalDateTime approvedDate;

// Rejection tracking
private User rejectedBy;
private LocalDateTime rejectedDate;
private String rejectReason; // Already exists

// Pickup tracking
private User pickupConfirmedBy;
private LocalDateTime actualPickupDate;

// Return tracking (dual confirmation)
private Boolean returnConfirmedByCustomer = false;
private LocalDateTime customerReturnDate;
private Boolean returnConfirmedByOwner = false;
private LocalDateTime ownerConfirmDate;
private LocalDateTime actualReturnDate;

// Getters and Setters...
```

### **Payment.java - Thêm fields:**

```java
// Money holding (Staff holds temporarily)
private User heldBy;
private LocalDateTime heldDate;

// Transfer to owner
private Boolean transferredToOwner = false;
private LocalDateTime transferDate;
private User transferredBy;

// Refund
private LocalDateTime refundDate;
private User refundedBy;
private BigDecimal refundAmount;

// Getters and Setters...
```

### **BookingStatus Enum - Update:**

```java
public enum BookingStatus {
    PendingPayment,          // Chờ thanh toán
    PendingOwnerApproval,    // Chờ owner duyệt
    Approved,                // Đã duyệt
    Rejected,                // Bị từ chối
    Ongoing,                 // Đang thuê
    Completed,               // Hoàn thành
    Cancelled                // Hủy
}
```

---

## 🔧 **Bước 3: Create Endpoints**

### **StaffController.java:**

```java
// 1. Xác nhận nhận tiền từ customer
@PostMapping("/booking/{bookingId}/confirm-payment-received")
public String confirmPaymentReceived(@PathVariable String bookingId,
                                    @RequestParam(required = false) String notes,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
    // Logic:
    // 1. Update Payment: status = Held, heldBy = currentStaff, heldDate = now
    // 2. Update Booking: status = PendingOwnerApproval, paymentStatus = "Held"
    // 3. Notify Owner: "New booking needs approval"
}

// 2. Refund tiền cho customer (khi owner reject)
@PostMapping("/booking/{bookingId}/refund-payment")
public String refundPayment(@PathVariable String bookingId,
                           @RequestParam BigDecimal refundAmount,
                           @RequestParam(required = false) String notes,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    // Logic:
    // 1. Update Payment: status = Refunded, refundDate = now, refundedBy = currentStaff
    // 2. Update Booking: paymentStatus = "Refunded"
    // 3. Notify Customer: "Your payment has been refunded"
}

// 3. Chuyển tiền cho owner (sau khi hoàn thành)
@PostMapping("/booking/{bookingId}/transfer-to-owner")
public String transferToOwner(@PathVariable String bookingId,
                             @RequestParam(required = false) String notes,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
    // Logic:
    // 1. Check: Both customer and owner confirmed return
    // 2. Update Payment: status = Completed, transferredToOwner = true, transferDate = now
    // 3. Update Booking: paymentStatus = "PaidToOwner"
    // 4. Notify Owner: "You have received payment for booking {code}"
}

// 4. Xác nhận giao xe
@PostMapping("/booking/{bookingId}/confirm-pickup")
public String confirmPickup(@PathVariable String bookingId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    // Logic:
    // 1. Update Booking: status = Ongoing, pickupConfirmedBy = currentStaff, actualPickupDate = now
    // 2. Notify Customer: "Enjoy your trip!"
}
```

### **OwnerController.java (NEW):**

```java
@Controller
@RequestMapping("/owner")
public class OwnerController {

    // 1. Approve booking
    @PostMapping("/booking/{bookingId}/approve")
    public String approveBooking(@PathVariable String bookingId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Logic:
        // 1. Update Booking: status = Approved, approvedBy = currentOwner, approvedDate = now
        // 2. Notify Customer: "Your booking has been approved!"
        // 3. Notify Staff: "Booking approved, ready for pickup"
    }

    // 2. Reject booking
    @PostMapping("/booking/{bookingId}/reject")
    public String rejectBooking(@PathVariable String bookingId,
                               @RequestParam String rejectReason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Logic:
        // 1. Update Booking: status = Rejected, rejectedBy = currentOwner, rejectReason, rejectedDate = now
        // 2. Update Payment: status = Refunding
        // 3. Notify Staff: "Booking rejected, please refund customer"
        // 4. Notify Customer: "Your booking was rejected: {reason}"
    }

    // 3. Xác nhận nhận xe trả lại
    @PostMapping("/booking/{bookingId}/confirm-return")
    public String confirmReturn(@PathVariable String bookingId,
                               @RequestParam(required = false) String notes,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Logic:
        // 1. Update Booking: returnConfirmedByOwner = true, ownerConfirmDate = now
        // 2. Check if customer also confirmed
        // 3. If both confirmed: actualReturnDate = now, status = Completed, paymentStatus = PendingTransferToOwner
        // 4. Notify Staff: "Both parties confirmed return, please transfer money to owner"
    }
}
```

### **CustomerController.java - Add:**

```java
// Xác nhận đã trả xe
@PostMapping("/booking/{bookingId}/confirm-return")
public String confirmReturn(@PathVariable String bookingId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    // Logic:
    // 1. Update Booking: returnConfirmedByCustomer = true, customerReturnDate = now
    // 2. Check if owner also confirmed
    // 3. If both confirmed: actualReturnDate = now, status = Completed
    // 4. Notify Owner: "Customer confirmed return, please check vehicle"
}
```

---

## 🎨 **Bước 4: Update UI**

### **Staff Dashboard:**

**Tabs:**
1. **Pending Payment** - Bookings chờ customer chuyển tiền
2. **Payment Received** - Đã nhận tiền, chờ owner duyệt
3. **Approved** - Owner đã duyệt, sẵn sàng giao xe
4. **Ongoing** - Đang thuê
5. **Pending Transfer** - Hoàn thành, chờ chuyển tiền cho owner
6. **Need Refund** - Cần hoàn tiền (owner reject)

**Actions:**
- ✅ Confirm Payment Received
- ✅ Refund Payment
- ✅ Transfer to Owner
- ✅ Confirm Pickup

### **Owner Dashboard (NEW):**

**Tabs:**
1. **Pending Approval** - Bookings chờ duyệt
2. **Approved** - Đã duyệt
3. **Ongoing** - Đang cho thuê
4. **Pending Return** - Chờ xác nhận trả xe
5. **Completed** - Hoàn thành

**Actions:**
- ✅ Approve Booking
- ✅ Reject Booking
- ✅ Confirm Vehicle Return

### **Customer Dashboard:**

**My Bookings - Status Display:**
- 🟡 **Pending Payment** - "Vui lòng thanh toán"
- 🟠 **Pending Owner Approval** - "Đang chờ chủ xe duyệt"
- 🟢 **Approved** - "Đã được duyệt. Có thể nhận xe vào {date}"
- 🔵 **Ongoing** - "Đang thuê. Nhớ trả xe đúng hạn"
- ⚪ **Completed** - "Hoàn thành"
- 🔴 **Rejected** - "Bị từ chối: {reason}. Đã hoàn tiền"

**Actions:**
- ✅ Confirm Vehicle Return

---

## 📊 **Bước 5: Create Services**

### **PaymentFlowService.java (NEW):**

```java
@Service
public class PaymentFlowService {

    // Staff confirms payment received
    public void confirmPaymentReceived(String bookingId, User staff);

    // Owner approves booking
    public void approveBooking(String bookingId, User owner);

    // Owner rejects booking
    public void rejectBooking(String bookingId, User owner, String reason);

    // Staff refunds payment
    public void refundPayment(String bookingId, User staff, BigDecimal amount);

    // Staff confirms pickup
    public void confirmPickup(String bookingId, User staff);

    // Customer confirms return
    public void customerConfirmReturn(String bookingId, User customer);

    // Owner confirms return
    public void ownerConfirmReturn(String bookingId, User owner);

    // Staff transfers money to owner
    public void transferToOwner(String bookingId, User staff);

    // Check if both parties confirmed return
    public boolean isBothPartiesConfirmedReturn(Booking booking);
}
```

---

## 🔔 **Bước 6: Notifications**

### **NotificationService.java:**

```java
// Notify owner about new booking
void notifyOwnerNewBooking(Booking booking);

// Notify customer about approval
void notifyCustomerBookingApproved(Booking booking);

// Notify customer about rejection
void notifyCustomerBookingRejected(Booking booking, String reason);

// Notify staff about rejection (need refund)
void notifyStaffNeedRefund(Booking booking);

// Notify customer about refund
void notifyCustomerRefunded(Booking booking);

// Notify owner about return confirmation
void notifyOwnerReturnConfirmed(Booking booking);

// Notify staff about both parties confirmed
void notifyStaffBothConfirmed(Booking booking);

// Notify owner about payment received
void notifyOwnerPaymentReceived(Booking booking);
```

---

## ✅ **Testing Checklist**

### **Happy Path:**
- [ ] Customer creates booking
- [ ] Customer pays
- [ ] Staff confirms payment received
- [ ] Owner receives notification
- [ ] Owner approves booking
- [ ] Customer receives approval notification
- [ ] Staff confirms pickup
- [ ] Booking status = Ongoing
- [ ] Customer confirms return
- [ ] Owner confirms return
- [ ] Booking status = Completed
- [ ] Staff transfers money to owner
- [ ] Owner receives payment notification

### **Reject Path:**
- [ ] Customer creates booking
- [ ] Customer pays
- [ ] Staff confirms payment received
- [ ] Owner rejects booking
- [ ] Staff receives refund notification
- [ ] Staff processes refund
- [ ] Customer receives refund notification
- [ ] Booking status = Rejected

---

## 🎯 **Summary**

**Files Created:**
1. ✅ `payment-flow-v2-migration.sql` - Database migration
2. ✅ `run-payment-flow-v2-migration.bat` - Migration script
3. ✅ `PAYMENT_FLOW_V2.md` - Detailed flow documentation
4. ✅ `IMPLEMENTATION_GUIDE.md` - This file

**Next Steps:**
1. Run migration script
2. Update Java models
3. Create endpoints
4. Update UI
5. Test thoroughly

**Estimated Time:**
- Phase 1 (Core): 2-3 days
- Phase 2 (Return Flow): 1-2 days
- Phase 3 (UI): 2-3 days
- Phase 4 (Testing): 1-2 days
- **Total: 6-10 days**

Bạn có muốn tôi bắt đầu implement code không? 🚀
