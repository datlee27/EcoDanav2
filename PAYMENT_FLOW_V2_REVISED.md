# Payment Flow V2 - REVISED (Sử dụng Database Hiện Có)

## 🎯 **Chiến lược mới:**

Sử dụng **ĐÚNG** các bảng có sẵn trong database thay vì thêm columns mới.

---

## 📊 **Mapping với Database hiện có:**

### **1. Booking Table (có sẵn)**
```sql
- Status: ENUM('Pending', 'Approved', 'Rejected', 'Ongoing', 'Completed', 'Cancelled')
- HandledBy: Staff xử lý
- PaymentStatus: String (custom field)
```

### **2. Payment Table (có sẵn)**
```sql
- PaymentStatus: ENUM('Pending', 'Completed', 'Failed', 'Refunded')
- PaymentType: ENUM('Deposit', 'FinalPayment', 'Surcharge', 'Refund')
- PaymentMethod: varchar(50)
- PaymentDate: datetime
- UserId: Staff/Owner xử lý
- Notes: Ghi chú
```

### **3. BookingApproval Table (có sẵn)**
```sql
- ApprovalStatus: ENUM('Approved', 'Rejected')
- StaffId: Staff duyệt (hoặc Owner)
- ApprovalDate: Ngày duyệt
- Note: Ghi chú
- RejectionReason: Lý do từ chối
```

---

## 🔄 **Luồng thanh toán mới:**

### **Step 1: Customer đặt xe**
```
Booking:
  Status = 'Pending'
  PaymentStatus = 'Unpaid'

Payment:
  PaymentStatus = 'Pending'
  PaymentType = 'Deposit'
```

### **Step 2: Staff xác nhận nhận tiền**
```
Payment:
  PaymentStatus = 'Completed'
  PaymentDate = NOW()
  UserId = StaffId
  Notes = "Đã nhận chuyển khoản"

Booking:
  PaymentStatus = 'Paid'
  HandledBy = StaffId
```

### **Step 3: Owner approve/reject**

**Nếu Approve:**
```
BookingApproval:
  ApprovalStatus = 'Approved'
  StaffId = OwnerId (hoặc tạo field OwnerId riêng)
  ApprovalDate = NOW()

Booking:
  Status = 'Approved'
```

**Nếu Reject:**
```
BookingApproval:
  ApprovalStatus = 'Rejected'
  StaffId = OwnerId
  RejectionReason = "Xe đang bảo trì"

Booking:
  Status = 'Rejected'
  PaymentStatus = 'Refunding'
```

### **Step 4: Staff giao xe (nếu approved)**
```
Booking:
  Status = 'Ongoing'
```

### **Step 5: Trả xe**
```
Booking:
  Status = 'Completed'
```

### **Step 6: Staff chuyển tiền cho Owner**
```
Payment (new record):
  PaymentType = 'FinalPayment'
  PaymentStatus = 'Completed'
  PaymentMethod = 'BankTransfer'
  UserId = StaffId
  Notes = "Chuyển tiền cho chủ xe"
  Amount = TotalAmount - Deposit

Booking:
  PaymentStatus = 'PaidToOwner'
```

### **Step 6b: Staff refund (nếu rejected)**
```
Payment (new record):
  PaymentType = 'Refund'
  PaymentStatus = 'Completed'
  UserId = StaffId
  Notes = "Hoàn tiền do owner từ chối"
  Amount = DepositAmount

Booking:
  PaymentStatus = 'Refunded'
```

---

## 📝 **PaymentStatus Values (Custom String):**

```
- "Unpaid" - Chưa thanh toán
- "Paid" - Đã thanh toán (Staff đã nhận)
- "PendingApproval" - Chờ owner duyệt
- "Approved" - Owner đã duyệt
- "Refunding" - Đang hoàn tiền
- "Refunded" - Đã hoàn tiền
- "PendingTransferToOwner" - Chờ chuyển tiền cho owner
- "PaidToOwner" - Đã chuyển tiền cho owner
```

---

## 🎯 **Ưu điểm:**

✅ **KHÔNG CẦN migration** - Sử dụng database hiện có
✅ **Tận dụng BookingApproval** - Đúng mục đích thiết kế
✅ **Payment history đầy đủ** - Mỗi transaction là 1 record
✅ **Dễ query** - JOIN với BookingApproval để lấy thông tin approve/reject
✅ **Backward compatible** - Không ảnh hưởng code cũ

---

## 📋 **Endpoints cần sửa:**

### **Staff Controller:**
1. `POST /staff/booking/{id}/confirm-payment-received`
   - Update Payment: `PaymentStatus = 'Completed'`
   - Update Booking: `PaymentStatus = 'Paid'`

2. `POST /staff/booking/{id}/confirm-pickup`
   - Update Booking: `Status = 'Ongoing'`

3. `POST /staff/booking/{id}/refund-payment`
   - Create new Payment: `PaymentType = 'Refund'`
   - Update Booking: `PaymentStatus = 'Refunded'`

4. `POST /staff/booking/{id}/transfer-to-owner`
   - Create new Payment: `PaymentType = 'FinalPayment'`
   - Update Booking: `PaymentStatus = 'PaidToOwner'`

### **Owner Controller:**
1. `POST /owner/booking/{id}/approve`
   - Create BookingApproval: `ApprovalStatus = 'Approved'`
   - Update Booking: `Status = 'Approved'`

2. `POST /owner/booking/{id}/reject`
   - Create BookingApproval: `ApprovalStatus = 'Rejected'`
   - Update Booking: `Status = 'Rejected'`, `PaymentStatus = 'Refunding'`

3. `POST /owner/booking/{id}/confirm-return`
   - Update Booking: `Status = 'Completed'`
   - Update Booking: `PaymentStatus = 'PendingTransferToOwner'`

### **Customer Controller:**
1. `POST /booking/{id}/confirm-return`
   - Update Booking: Add note or flag (có thể dùng Notes field)

---

## 🔍 **Query Examples:**

### **Lấy thông tin approval:**
```java
BookingApproval approval = bookingApprovalRepository.findByBookingId(bookingId);
if (approval != null && approval.getApprovalStatus() == ApprovalStatus.Approved) {
    // Owner đã approve
}
```

### **Lấy payment history:**
```java
List<Payment> payments = paymentRepository.findByBookingId(bookingId);
// payments[0] = Deposit (from customer)
// payments[1] = FinalPayment (to owner) hoặc Refund (to customer)
```

---

## ✅ **Kết luận:**

Cách này **KHÔNG CẦN migration**, sử dụng đúng cấu trúc database hiện có, và vẫn đáp ứng đầy đủ yêu cầu của bạn!

Bạn có muốn tôi implement lại các controllers theo cách này không?
