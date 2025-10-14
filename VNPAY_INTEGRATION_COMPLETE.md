# VNPay Integration - Complete Guide

## ✅ Luồng hoàn chỉnh từ đầu đến cuối

### **1. Khách hàng đặt xe**
**File:** `BookingController.java` → `createBooking()`

**Flow:**
1. User điền form đặt xe (ngày giờ, xe, discount)
2. Click "Xác nhận đặt xe"
3. Controller validate data
4. Tạo `Booking` với status `Pending`
5. Tạo `BookingCode` (ví dụ: `BK1730000000000`)
6. Save vào database
7. **Redirect:** `/payment/select-method/{bookingId}`

**Code:**
```java
booking.setStatus(Booking.BookingStatus.Pending);
booking.setBookingCode("BK" + System.currentTimeMillis());
booking.setPaymentStatus("Unpaid");
bookingService.addBooking(booking);
return "redirect:/payment/select-method/" + booking.getBookingId();
```

---

### **2. Chọn phương thức thanh toán**
**File:** `payment-method-selection.html`

**Các phương thức:**
- ✅ **Tiền mặt** → Thanh toán khi nhận xe
- ✅ **Chuyển khoản** → Hiển thị thông tin ngân hàng
- ✅ **Mã QR** → Quét QR để thanh toán
- ✅ **VNPay** → Redirect đến VNPay gateway
- ⚠️ **Thẻ tín dụng** → Đang phát triển

**JavaScript:**
```javascript
document.addEventListener('DOMContentLoaded', function() {
    const paymentCards = document.querySelectorAll('.payment-method-card');
    
    paymentCards.forEach(card => {
        card.addEventListener('click', function() {
            selectedMethod = this.dataset.method;
            // Update UI
        });
    });
});
```

**Form submit:**
```html
<form action="/payment/process" method="post">
    <input type="hidden" name="bookingId" value="${booking.bookingId}">
    <input type="hidden" name="paymentMethod" id="selectedPaymentMethod">
</form>
```

---

### **3. Xử lý thanh toán VNPay**
**File:** `PaymentMethodController.java` → `processVNPayPayment()`

**Flow:**
1. Nhận `bookingId` và `paymentMethod=VNPAY`
2. Tạo `Payment` record với status `Pending`
3. Generate VNPay payment URL
4. Redirect user đến VNPay

**Code:**
```java
private String processVNPayPayment(Booking booking, User user, HttpServletRequest request, RedirectAttributes redirectAttributes) {
    // Create payment record
    paymentService.createPayment(
        booking,
        user,
        booking.getTotalAmount(),
        "VNPay",
        Payment.PaymentType.Deposit,
        booking.getBookingCode() // Transaction ID
    );

    // Generate VNPay URL
    String paymentUrl = vnPayService.createPaymentUrl(booking, request);
    
    return "redirect:" + paymentUrl;
}
```

---

### **4. Generate VNPay Payment URL**
**File:** `VNPayService.java` → `createPaymentUrl()`

**Parameters gửi đến VNPay:**
```java
vnp_Version: 2.1.0
vnp_Command: pay
vnp_TmnCode: NJRKMHYG (sandbox)
vnp_Amount: {totalAmount * 100} // VNPay yêu cầu nhân 100
vnp_CurrCode: VND
vnp_TxnRef: {bookingCode} // Mã đơn hàng
vnp_OrderInfo: "Thanh toan dat xe {bookingCode}"
vnp_OrderType: other
vnp_Locale: vn
vnp_ReturnUrl: http://localhost:8080/payment/vnpay-return
vnp_IpAddr: {client IP}
vnp_CreateDate: yyyyMMddHHmmss
vnp_ExpireDate: yyyyMMddHHmmss (+ 15 phút)
vnp_SecureHash: HMAC-SHA512 hash
```

**Hash calculation:**
```java
// 1. Sort parameters alphabetically
// 2. Build hash data: key1=value1&key2=value2&...
// 3. HMAC-SHA512(hashSecret, hashData)
String hashData = VNPayConfig.buildHashData(vnpParams);
String vnpSecureHash = VNPayConfig.hmacSHA512(hashSecret, hashData);
```

**Final URL:**
```
https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=96040100&vnp_Command=pay&...&vnp_SecureHash=abc123...
```

---

### **5. User thanh toán trên VNPay**
1. User được redirect đến VNPay sandbox
2. Chọn ngân hàng (NCB, VCB, etc.)
3. Nhập thông tin thẻ test:
   - Số thẻ: `9704198526191432198`
   - Tên: `NGUYEN VAN A`
   - Ngày phát hành: `07/15`
   - Mật khẩu OTP: `123456`
4. Xác nhận thanh toán

---

### **6. VNPay callback - Return URL**
**File:** `VNPayController.java` → `vnpayReturn()`

**VNPay trả về parameters:**
```
vnp_TxnRef: BK1730000000000 (booking code)
vnp_ResponseCode: 00 (success) hoặc khác (fail)
vnp_TransactionNo: 14398888 (VNPay transaction ID)
vnp_Amount: 96040100
vnp_BankCode: NCB
vnp_PayDate: 20251013223000
vnp_SecureHash: abc123...
```

**⚠️ QUAN TRỌNG:** VNPay trả về **CẢ 2 tham số**:
- `vnp_ResponseCode` - Mã phản hồi kết quả thanh toán
- `vnp_TransactionStatus` - Tình trạng giao dịch tại VNPAY

**Cần check CẢ 2 để xác nhận thành công:**
- `vnp_ResponseCode = "00"` AND `vnp_TransactionStatus = "00"` → Thành công

**vnp_ResponseCode:**
- `00`: Giao dịch thành công
- `07`: Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)
- `09`: Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng
- `10`: Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần
- `11`: Giao dịch không thành công do: Đã hết hạn chờ thanh toán
- `12`: Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa
- `13`: Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)
- `24`: Giao dịch không thành công do: Khách hàng hủy giao dịch
- `51`: Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch
- `65`: Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày
- `75`: Ngân hàng thanh toán đang bảo trì
- `79`: Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định
- `99`: Các lỗi khác

**vnp_TransactionStatus:**
- `00`: Giao dịch thành công
- `01`: Giao dịch chưa hoàn tất
- `02`: Giao dịch bị lỗi
- `04`: Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)
- `05`: VNPAY đang xử lý giao dịch này (GD hoàn tiền)
- `06`: VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)
- `07`: Giao dịch bị nghi ngờ gian lận
- `09`: GD Hoàn trả bị từ chối

**Flow xử lý:**
```java
@GetMapping("/vnpay-return")
public String vnpayReturn(HttpServletRequest request, Model model) {
    // 1. Get all parameters
    Map<String, String> params = vnPayService.getParamsFromRequest(request);
    
    // 2. Validate signature
    String vnpSecureHash = params.get("vnp_SecureHash");
    if (!vnPayService.validateSignature(params, vnpSecureHash)) {
        // Invalid signature
        return "customer/payment-result";
    }
    
    // 3. Get transaction info
    String vnpTxnRef = params.get("vnp_TxnRef");
    String vnpResponseCode = params.get("vnp_ResponseCode");
    String vnpTransactionStatus = params.get("vnp_TransactionStatus");
    String vnpTransactionNo = params.get("vnp_TransactionNo");
    
    // 4. Find booking by booking code
    Booking booking = findByBookingCode(vnpTxnRef);
    
    // 5. Find payment record
    Payment payment = findByTransactionId(vnpTxnRef);
    
    // 6. Update payment status
    // MUST check BOTH vnp_ResponseCode AND vnp_TransactionStatus
    if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
        // SUCCESS
        payment.setPaymentStatus(Payment.PaymentStatus.Completed);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNotes("Thanh toán thành công qua VNPay. Mã GD: " + vnpTransactionNo);
        
        // Update booking
        booking.setStatus(Booking.BookingStatus.Approved);
        booking.setPaymentStatus("Paid");
        
        model.addAttribute("success", true);
    } else {
        // FAILED
        payment.setPaymentStatus(Payment.PaymentStatus.Failed);
        payment.setNotes("Thanh toán thất bại. Mã lỗi: " + vnpResponseCode);
        
        model.addAttribute("error", true);
    }
    
    // 7. Save changes
    paymentService.savePayment(payment);
    bookingService.updateBooking(booking);
    
    return "customer/payment-result";
}
```

---

### **7. VNPay IPN (Instant Payment Notification)**
**File:** `VNPayController.java` → `vnpayIPN()`

**Purpose:** Server-to-server notification từ VNPay

**Response format:**
```json
{
    "RspCode": "00",
    "Message": "Confirm Success"
}
```

**Response codes:**
- `00`: Success
- `01`: Order not found
- `02`: Order already confirmed
- `97`: Invalid signature
- `99`: Unknown error

---

## 🔧 Configuration

### **application.properties**
```properties
# VNPay Sandbox Configuration
vnpay.tmn-code=NJRKMHYG
vnpay.hash-secret=Q1UOEXJQEWI849T3876HS9GQ19B3E89F
vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=http://localhost:8080/payment/vnpay-return
vnpay.ipn-url=http://localhost:8080/payment/vnpay-ipn
vnpay.version=2.1.0
vnpay.command=pay
vnpay.currency-code=VND
```

### **Database Migration**
```sql
-- Add PaymentStatus column to Booking table
ALTER TABLE Booking 
ADD COLUMN PaymentStatus VARCHAR(20) DEFAULT 'Unpaid';

-- Create indexes
CREATE INDEX idx_booking_payment_status ON Booking(PaymentStatus);
CREATE INDEX idx_booking_code ON Booking(BookingCode);
CREATE INDEX idx_payment_booking ON payment(BookingId);
CREATE INDEX idx_payment_transaction ON payment(TransactionId);
```

---

## 🧪 Testing

### **Test Cards (VNPay Sandbox)**
```
Ngân hàng: NCB
Số thẻ: 9704198526191432198
Tên: NGUYEN VAN A
Ngày phát hành: 07/15
Mật khẩu OTP: 123456
```

### **Test Flow**
1. ✅ Đặt xe → Tạo booking
2. ✅ Chọn VNPay
3. ✅ Redirect đến VNPay sandbox
4. ✅ Nhập thông tin thẻ test
5. ✅ Xác nhận OTP
6. ✅ VNPay redirect về `/payment/vnpay-return`
7. ✅ Hiển thị kết quả thanh toán
8. ✅ Booking status → `Approved`
9. ✅ Payment status → `Completed`

---

## ⚠️ Common Issues

### **Issue 1: Invalid Signature**
**Cause:** Hash calculation sai
**Fix:** 
- Check `hashSecret` đúng không
- Check parameters được sort alphabetically
- Check encoding (UTF-8 vs US-ASCII)

### **Issue 2: Amount mismatch**
**Cause:** VNPay yêu cầu amount × 100
**Fix:**
```java
long amount = booking.getTotalAmount().multiply(new BigDecimal(100)).longValue();
```

### **Issue 3: Transaction not found**
**Cause:** `vnp_TxnRef` không match với `BookingCode`
**Fix:** Ensure `vnp_TxnRef = booking.getBookingCode()`

### **Issue 4: Payment already processed**
**Cause:** IPN và Return URL đều update payment
**Fix:** Check payment status trước khi update

---

## 📊 Database Schema

### **Booking Table**
```sql
BookingId (PK)
UserId (FK)
VehicleId (FK)
BookingCode (UNIQUE)
TotalAmount
Status (Pending, Approved, Rejected, Ongoing, Completed, Cancelled)
PaymentStatus (Unpaid, Paid, Refunded, Partial)
CreatedDate
```

### **Payment Table**
```sql
PaymentId (PK)
BookingId (FK)
UserId (FK)
Amount
PaymentMethod (Cash, Bank Transfer, QR Code, VNPay, Credit Card)
PaymentStatus (Pending, Completed, Failed, Refunded)
PaymentType (Deposit, FinalPayment, Surcharge, Refund)
TransactionId (Booking Code for VNPay)
PaymentDate
Notes
CreatedDate
```

---

## 🎯 Summary

**Luồng chính:**
1. Booking → Payment Selection → VNPay
2. VNPay → Return URL → Update Status
3. Display Result

**Key Points:**
- ✅ Use `BookingCode` as `vnp_TxnRef`
- ✅ Amount × 100 for VNPay
- ✅ **Check BOTH `vnp_ResponseCode` AND `vnp_TransactionStatus`** (cả 2 phải = "00")
- ✅ Validate signature on return
- ✅ Handle both Return URL and IPN
- ✅ Update both Booking and Payment status

**Status Flow:**
```
Booking: Pending → Approved (when payment success)
Payment: Pending → Completed (when payment success)
```
