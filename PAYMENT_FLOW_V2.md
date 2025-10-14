# Payment Flow V2 - Three-Party System

## 👥 **Vai trò:**

1. **Customer** - Người thuê xe
2. **Staff** - Quản lý thanh toán, giữ tiền
3. **Owner** - Chủ xe, quyết định cho thuê

---

## 🔄 **Luồng hoàn chỉnh:**

### **Bước 1: Customer đặt xe và chuyển tiền**

**Action:** Customer chọn xe → Đặt xe → Chọn phương thức thanh toán → Chuyển tiền

**Status:**
```
Booking.status = PendingPayment
Booking.paymentStatus = Pending
Payment.paymentStatus = Pending
Payment.heldBy = Staff
```

**Tiền:** Customer → Staff (giữ tạm)

---

### **Bước 2: Staff xác nhận nhận tiền**

**Action:** Staff check bank statement → Xác nhận đã nhận tiền

**Status:**
```
Booking.status = PendingOwnerApproval
Booking.paymentStatus = Held (Staff đang giữ)
Payment.paymentStatus = Held
Payment.heldBy = Staff
Payment.heldDate = LocalDateTime.now()
```

**Notification:** Gửi thông báo tới Owner: "Có booking mới cần duyệt"

---

### **Bước 3: Owner xem và quyết định**

#### **3A. Owner APPROVE:**

**Action:** Owner xem booking → Approve

**Status:**
```
Booking.status = Approved
Booking.paymentStatus = Held
Booking.approvedBy = Owner
Booking.approvedDate = LocalDateTime.now()
```

**Notification:** Gửi thông báo tới Customer: "Booking đã được chấp nhận. Bạn có thể nhận xe."

**Tiền:** Vẫn giữ ở Staff

---

#### **3B. Owner REJECT:**

**Action:** Owner xem booking → Reject (với lý do)

**Status:**
```
Booking.status = Rejected
Booking.paymentStatus = Refunding
Booking.rejectedBy = Owner
Booking.rejectReason = "Xe đang bảo trì"
Payment.paymentStatus = Refunding
```

**Action tiếp theo:** Staff refund tiền cho Customer

**Status sau refund:**
```
Booking.status = Rejected
Booking.paymentStatus = Refunded
Payment.paymentStatus = Refunded
Payment.refundDate = LocalDateTime.now()
```

**Notification:** Gửi thông báo tới Customer: "Booking bị từ chối. Tiền đã được hoàn lại."

**Tiền:** Staff → Customer (refund)

---

### **Bước 4: Customer nhận xe**

**Action:** Customer đến nhận xe → Staff/Owner xác nhận giao xe

**Status:**
```
Booking.status = Ongoing
Booking.pickupConfirmedBy = Staff/Owner
Booking.actualPickupDate = LocalDateTime.now()
```

**Tiền:** Vẫn giữ ở Staff

---

### **Bước 5: Customer trả xe**

**Action:** Customer trả xe → Cần CẢ Customer VÀ Owner xác nhận

#### **5A. Customer xác nhận trả xe:**

**Status:**
```
Booking.returnConfirmedByCustomer = true
Booking.customerReturnDate = LocalDateTime.now()
```

#### **5B. Owner xác nhận nhận xe:**

**Status:**
```
Booking.returnConfirmedByOwner = true
Booking.ownerConfirmDate = LocalDateTime.now()
```

#### **5C. Cả 2 đã xác nhận → Hoàn thành:**

**Status:**
```
Booking.status = Completed
Booking.paymentStatus = PendingTransferToOwner
Booking.actualReturnDate = LocalDateTime.now()
```

**Action tiếp theo:** Staff chuyển tiền cho Owner

---

### **Bước 6: Staff chuyển tiền cho Owner**

**Action:** Staff xác nhận đã chuyển tiền cho Owner

**Status:**
```
Booking.paymentStatus = PaidToOwner
Payment.paymentStatus = Completed
Payment.transferredToOwner = true
Payment.transferDate = LocalDateTime.now()
Payment.transferredBy = Staff
```

**Tiền:** Staff → Owner

**Notification:** Gửi thông báo tới Owner: "Đã nhận tiền từ booking {bookingCode}"

---

## 📊 **Database Schema Updates**

### **Booking Table - Thêm columns:**

```sql
ALTER TABLE Booking ADD COLUMN ApprovedBy CHAR(36) NULL;
ALTER TABLE Booking ADD COLUMN ApprovedDate DATETIME NULL;
ALTER TABLE Booking ADD COLUMN RejectedBy CHAR(36) NULL;
ALTER TABLE Booking ADD COLUMN RejectReason VARCHAR(500) NULL;
ALTER TABLE Booking ADD COLUMN PickupConfirmedBy CHAR(36) NULL;
ALTER TABLE Booking ADD COLUMN ActualPickupDate DATETIME NULL;
ALTER TABLE Booking ADD COLUMN ReturnConfirmedByCustomer BOOLEAN DEFAULT FALSE;
ALTER TABLE Booking ADD COLUMN CustomerReturnDate DATETIME NULL;
ALTER TABLE Booking ADD COLUMN ReturnConfirmedByOwner BOOLEAN DEFAULT FALSE;
ALTER TABLE Booking ADD COLUMN OwnerConfirmDate DATETIME NULL;
ALTER TABLE Booking ADD COLUMN ActualReturnDate DATETIME NULL;

-- Foreign keys
ALTER TABLE Booking ADD CONSTRAINT fk_approved_by FOREIGN KEY (ApprovedBy) REFERENCES Users(UserId);
ALTER TABLE Booking ADD CONSTRAINT fk_rejected_by FOREIGN KEY (RejectedBy) REFERENCES Users(UserId);
ALTER TABLE Booking ADD CONSTRAINT fk_pickup_confirmed_by FOREIGN KEY (PickupConfirmedBy) REFERENCES Users(UserId);
```

### **Payment Table - Thêm columns:**

```sql
ALTER TABLE payment ADD COLUMN HeldBy CHAR(36) NULL;
ALTER TABLE payment ADD COLUMN HeldDate DATETIME NULL;
ALTER TABLE payment ADD COLUMN TransferredToOwner BOOLEAN DEFAULT FALSE;
ALTER TABLE payment ADD COLUMN TransferDate DATETIME NULL;
ALTER TABLE payment ADD COLUMN TransferredBy CHAR(36) NULL;
ALTER TABLE payment ADD COLUMN RefundDate DATETIME NULL;
ALTER TABLE payment ADD COLUMN RefundedBy CHAR(36) NULL;

-- Foreign keys
ALTER TABLE payment ADD CONSTRAINT fk_held_by FOREIGN KEY (HeldBy) REFERENCES Users(UserId);
ALTER TABLE payment ADD CONSTRAINT fk_transferred_by FOREIGN KEY (TransferredBy) REFERENCES Users(UserId);
ALTER TABLE payment ADD CONSTRAINT fk_refunded_by FOREIGN KEY (RefundedBy) REFERENCES Users(UserId);
```

### **Booking Status Enum - Cập nhật:**

```java
public enum BookingStatus {
    PendingPayment,      // Chờ customer thanh toán
    PendingOwnerApproval, // Chờ owner duyệt
    Approved,            // Owner đã duyệt
    Rejected,            // Owner từ chối
    Ongoing,             // Đang thuê
    Completed,           // Hoàn thành
    Cancelled            // Hủy
}
```

### **Payment Status - Cập nhật:**

```java
public enum PaymentStatus {
    Pending,             // Chờ thanh toán
    Held,                // Staff đang giữ tiền
    Refunding,           // Đang hoàn tiền
    Refunded,            // Đã hoàn tiền
    PendingTransferToOwner, // Chờ chuyển cho owner
    Completed            // Đã chuyển cho owner
}
```

---

## 🎯 **Endpoints cần tạo:**

### **Staff Endpoints:**

```java
// Xác nhận nhận tiền từ customer
POST /staff/booking/{bookingId}/confirm-payment-received

// Refund tiền cho customer (khi owner reject)
POST /staff/booking/{bookingId}/refund-payment

// Chuyển tiền cho owner (sau khi hoàn thành)
POST /staff/booking/{bookingId}/transfer-to-owner

// Xác nhận giao xe
POST /staff/booking/{bookingId}/confirm-pickup
```

### **Owner Endpoints:**

```java
// Approve booking
POST /owner/booking/{bookingId}/approve

// Reject booking
POST /owner/booking/{bookingId}/reject
  - rejectReason: String

// Xác nhận nhận xe trả lại
POST /owner/booking/{bookingId}/confirm-return
```

### **Customer Endpoints:**

```java
// Xác nhận đã trả xe
POST /customer/booking/{bookingId}/confirm-return
```

---

## 🔐 **Permission Matrix:**

| Action | Customer | Staff | Owner |
|--------|----------|-------|-------|
| Create Booking | ✅ | ❌ | ❌ |
| Pay | ✅ | ❌ | ❌ |
| Confirm Payment Received | ❌ | ✅ | ❌ |
| Approve Booking | ❌ | ❌ | ✅ |
| Reject Booking | ❌ | ❌ | ✅ |
| Refund Payment | ❌ | ✅ | ❌ |
| Confirm Pickup | ❌ | ✅ | ✅ |
| Confirm Return (Customer) | ✅ | ❌ | ❌ |
| Confirm Return (Owner) | ❌ | ❌ | ✅ |
| Transfer to Owner | ❌ | ✅ | ❌ |

---

## 📱 **UI Flow:**

### **Customer Dashboard:**

**My Bookings:**
- 🟡 Pending Payment - "Vui lòng thanh toán"
- 🟠 Pending Owner Approval - "Đang chờ chủ xe duyệt"
- 🟢 Approved - "Đã được duyệt. Có thể nhận xe"
- 🔵 Ongoing - "Đang thuê. Nhớ trả xe đúng hạn"
- ⚪ Completed - "Hoàn thành"
- 🔴 Rejected - "Bị từ chối. Đã hoàn tiền"

### **Staff Dashboard:**

**Tabs:**
1. **Pending Payment** - Chờ customer chuyển tiền
2. **Payment Received** - Đã nhận tiền, chờ owner duyệt
3. **Approved** - Owner đã duyệt
4. **Ongoing** - Đang thuê
5. **Completed** - Chờ chuyển tiền cho owner
6. **Need Refund** - Cần hoàn tiền

### **Owner Dashboard:**

**Tabs:**
1. **Pending Approval** - Chờ duyệt
2. **Approved** - Đã duyệt
3. **Ongoing** - Đang cho thuê
4. **Pending Return Confirm** - Chờ xác nhận trả xe
5. **Completed** - Hoàn thành

---

## 💰 **Money Flow:**

```
Customer → Staff (giữ tạm)
         ↓
    Owner approve?
         ↓
    Yes → Staff giữ tiền → Hoàn thành chuyến → Staff → Owner
         ↓
    No → Staff → Customer (refund)
```

---

## 🧪 **Test Cases:**

### **Happy Path:**
1. Customer đặt xe → Chuyển tiền
2. Staff xác nhận nhận tiền
3. Owner approve
4. Customer nhận xe
5. Customer trả xe
6. Owner xác nhận nhận xe
7. Staff chuyển tiền cho Owner
8. ✅ Hoàn thành

### **Reject Path:**
1. Customer đặt xe → Chuyển tiền
2. Staff xác nhận nhận tiền
3. Owner reject
4. Staff refund cho Customer
5. ❌ Booking bị hủy

---

## 📝 **Implementation Priority:**

### **Phase 1: Core Flow**
- [ ] Database migration
- [ ] Update Booking model
- [ ] Update Payment model
- [ ] Staff confirm payment endpoint
- [ ] Owner approve/reject endpoints

### **Phase 2: Return Flow**
- [ ] Customer confirm return endpoint
- [ ] Owner confirm return endpoint
- [ ] Staff transfer to owner endpoint

### **Phase 3: UI**
- [ ] Staff dashboard updates
- [ ] Owner dashboard
- [ ] Customer booking status display

### **Phase 4: Notifications**
- [ ] Email notifications
- [ ] In-app notifications
- [ ] SMS notifications (optional)

---

Bạn có muốn tôi bắt đầu implement không? 🚀
