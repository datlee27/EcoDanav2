# Payment Confirmation Flow - Bank Transfer & QR Code

## 📋 **Tổng quan**

Khi khách hàng chọn thanh toán bằng **Chuyển khoản** hoặc **Mã QR**, hệ thống không thể tự động xác nhận thanh toán. Staff cần xác nhận thủ công sau khi nhận được tiền.

---

## 🔄 **Luồng hoàn chỉnh**

### **1. Khách hàng chọn phương thức thanh toán**

**Trang:** `payment-method-selection.html`

**Các phương thức:**
- ✅ **Tiền mặt** → Thanh toán khi nhận xe (không cần xác nhận trước)
- ✅ **Chuyển khoản** → Cần Staff xác nhận
- ✅ **Mã QR** → Cần Staff xác nhận
- ✅ **VNPay** → Tự động xác nhận qua API
- ⚠️ **Thẻ tín dụng** → Đang phát triển

---

### **2. Hiển thị thông tin thanh toán**

**Trang:** `payment-bank-transfer.html`

**Trạng thái:** ⏳ **Đang chờ thanh toán**

**Thông tin hiển thị:**
```
Ngân hàng: Vietcombank - Chi nhánh Đà Nẵng
Số tài khoản: 1234567890
Chủ tài khoản: CÔNG TY ECODANA
Số tiền: 960,401 ₫
Nội dung: BK1730000000000 (Booking Code)
```

**Trạng thái trong DB:**
```java
Booking.status = Pending
Booking.paymentStatus = "Pending"
Payment.paymentStatus = Pending
```

---

### **3. Khách hàng chuyển tiền**

Khách hàng thực hiện chuyển khoản qua:
- Internet Banking
- Mobile Banking
- ATM
- Quét mã QR

**⚠️ Quan trọng:** Khách hàng phải ghi đúng nội dung chuyển khoản = **Booking Code**

---

### **4. Staff xác nhận thanh toán**

**Trang:** Staff Dashboard → Booking Details

**Endpoint:** `POST /staff/booking/{bookingId}/confirm-payment`

**Form:**
```html
<form action="/staff/booking/{bookingId}/confirm-payment" method="post">
    <input type="text" name="notes" placeholder="Ghi chú (tùy chọn)">
    <button type="submit">Xác nhận đã nhận tiền</button>
</form>
```

**Controller Logic:**
```java
@PostMapping("/booking/{bookingId}/confirm-payment")
public String confirmPayment(@PathVariable String bookingId,
                            @RequestParam(required = false) String notes,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
    // 1. Find booking
    Booking booking = bookingService.findById(bookingId);
    
    // 2. Find payment record
    Payment payment = paymentService.findByBookingId(bookingId);
    
    // 3. Update payment status
    payment.setPaymentStatus(Payment.PaymentStatus.Completed);
    payment.setPaymentDate(LocalDateTime.now());
    payment.setNotes(notes);
    paymentService.savePayment(payment);
    
    // 4. Update booking status
    booking.setPaymentStatus("Paid");
    booking.setStatus(Booking.BookingStatus.Approved);
    booking.setHandledBy(currentUser);
    bookingService.updateBooking(booking);
    
    return "redirect:/staff/booking/" + bookingId;
}
```

---

### **5. Hiển thị kết quả**

**Sau khi Staff xác nhận:**

**Trạng thái trong DB:**
```java
Booking.status = Approved
Booking.paymentStatus = "Paid"
Payment.paymentStatus = Completed
Payment.paymentDate = LocalDateTime.now()
```

**Thông báo cho khách hàng:**
- ✅ Email notification (nếu có)
- ✅ Hiển thị trong "My Bookings" với status "Approved"
- ✅ Có thể xem chi tiết booking

---

## 🎨 **UI/UX Flow**

### **Customer Side:**

1. **Chọn phương thức** → Click "Chuyển khoản"
2. **Xem thông tin** → Hiển thị thông tin ngân hàng + QR code
3. **Chuyển tiền** → Thực hiện chuyển khoản
4. **Chờ xác nhận** → Trạng thái "Đang chờ xác nhận"
5. **Nhận thông báo** → "Thanh toán thành công" (sau khi Staff xác nhận)

### **Staff Side:**

1. **Xem danh sách** → Pending Bookings
2. **Kiểm tra chuyển khoản** → Check bank statement
3. **Xác nhận** → Click "Xác nhận đã nhận tiền"
4. **Cập nhật** → Booking status → Approved

---

## 📊 **Database Schema**

### **Booking Table:**
```sql
BookingId (PK)
Status (Pending → Approved)
PaymentStatus (Pending → Paid)
HandledBy (Staff User ID)
```

### **Payment Table:**
```sql
PaymentId (PK)
BookingId (FK)
PaymentMethod (Bank Transfer / QR Code)
PaymentStatus (Pending → Completed)
PaymentDate (NULL → LocalDateTime)
TransactionId (Booking Code)
Notes (Staff notes)
```

---

## 🔧 **Implementation Checklist**

### ✅ **Backend:**
- [x] `PaymentService.findByBookingId()` method
- [x] `StaffController.confirmPayment()` endpoint
- [x] Update booking status logic
- [x] Update payment status logic

### ✅ **Frontend:**
- [x] `payment-bank-transfer.html` - Show "Pending" status
- [ ] `booking-details.html` (Staff) - Add "Confirm Payment" button
- [ ] Show payment status in Staff dashboard

### 📝 **TODO:**
- [ ] Add "Confirm Payment" button to Staff booking details page
- [ ] Add email notification when payment confirmed
- [ ] Add payment history view for customers
- [ ] Add filter for "Pending Payment" bookings in Staff dashboard

---

## 🧪 **Testing**

### **Test Case 1: Bank Transfer Flow**
1. Customer creates booking
2. Selects "Bank Transfer"
3. Views bank info page
4. Transfers money
5. Staff checks bank statement
6. Staff confirms payment
7. Booking status → Approved
8. Customer sees "Paid" status

### **Test Case 2: QR Code Flow**
1. Customer creates booking
2. Selects "QR Code"
3. Scans QR code
4. Transfers money
5. Staff confirms payment
6. Booking status → Approved

---

## 🎯 **Summary**

**Luồng chính:**
```
Customer → Select Payment → Transfer Money → Staff Confirm → Success
```

**Key Points:**
- ✅ Payment status starts as "Pending"
- ✅ Staff manually confirms after receiving money
- ✅ Booking status changes to "Approved" after confirmation
- ✅ Customer can track status in "My Bookings"
- ✅ Staff can see all pending payments in dashboard

**Status Flow:**
```
Booking: Pending → Approved (after payment confirmed)
Payment: Pending → Completed (after staff confirms)
```
