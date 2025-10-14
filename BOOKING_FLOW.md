# Luồng Đặt Xe và Thanh Toán - EcoDana v2

## 📋 Tổng quan

Hệ thống sử dụng **trạng thái kép** (Status + PaymentStatus) để quản lý booking:

### Trạng thái Booking (Status)
- `Pending` - Chờ thanh toán / Chờ Owner duyệt
- `Approved` - Owner đã duyệt
- `Rejected` - Owner từ chối
- `Ongoing` - Đang thuê
- `Completed` - Hoàn thành
- `Cancelled` - Đã hủy / Đã hoàn tiền

### Trạng thái Thanh toán (PaymentStatus)
- `Unpaid` - Chưa thanh toán
- `Paid` - Đã thanh toán
- `Refunded` - Đã hoàn tiền

---

## 🔄 Luồng nghiệp vụ chi tiết

### 1️⃣ Customer đặt xe

```
Customer chọn xe → Điền thông tin → Tạo booking
├─ Status: Pending
└─ PaymentStatus: Unpaid
```

### 2️⃣ Customer thanh toán VNPay

```
Customer thanh toán → VNPay xử lý → Redirect về
├─ Thành công (vnp_ResponseCode = 00):
│  ├─ Payment.status = Completed
│  ├─ Booking.paymentStatus = Paid
│  ├─ Booking.status = Pending (giữ nguyên)
│  └─ Gửi thông báo cho Owner
│
└─ Thất bại:
   └─ Hiển thị lỗi, giữ nguyên trạng thái
```

**File**: `PaymentMethodController.vnpayReturn()`, `PaymentMethodController.vnpayIPN()`

### 3️⃣ Owner xem danh sách booking chờ duyệt

```
Owner đăng nhập → /owner/bookings/pending-approval
└─ Hiển thị các booking: Status=Pending AND PaymentStatus=Paid
```

**File**: `OwnerBookingController.showPendingApprovalBookings()`

### 4️⃣ Owner duyệt booking

#### ✅ Trường hợp CHẤP NHẬN

```
Owner click "Chấp nhận"
├─ Booking.status = Approved
├─ Booking.handledBy = Owner
├─ Gửi thông báo cho Customer: "Đơn đặt xe đã được chấp nhận"
└─ Gửi thông báo cho Staff (nếu cần)
```

**File**: `OwnerBookingController.approveBooking()`

#### ❌ Trường hợp TỪ CHỐI

```
Owner click "Từ chối" + nhập lý do
├─ Booking.status = Rejected
├─ Booking.cancelReason = rejectionReason
├─ Tạo Payment mới:
│  ├─ PaymentType = Refund
│  ├─ PaymentStatus = Completed
│  └─ Notes = "Hoàn tiền do Owner từ chối: [lý do]"
├─ Booking.paymentStatus = Refunded
├─ Booking.status = Cancelled
├─ Gửi thông báo cho Customer: "Đơn đặt xe bị từ chối, đã hoàn tiền"
└─ Gửi thông báo cho Staff về việc hoàn tiền
```

**File**: `OwnerBookingController.rejectBooking()`

---

## 📊 Ma trận trạng thái

| Tình huống | Status | PaymentStatus | Ý nghĩa |
|-----------|--------|---------------|---------|
| Vừa tạo booking | `Pending` | `Unpaid` | Chờ thanh toán |
| Đã thanh toán, chờ duyệt | `Pending` | `Paid` | Chờ Owner duyệt |
| Owner chấp nhận | `Approved` | `Paid` | Đã duyệt, sẵn sàng giao xe |
| Owner từ chối | `Cancelled` | `Refunded` | Đã hủy và hoàn tiền |
| Customer hủy trước khi thanh toán | `Cancelled` | `Unpaid` | Hủy không hoàn tiền |
| Đang thuê | `Ongoing` | `Paid` | Xe đang được thuê |
| Hoàn thành | `Completed` | `Paid` | Đã trả xe |

---

## 🎯 Điểm quan trọng

### 1. Phân biệt booking chờ duyệt
```java
// Booking chờ Owner duyệt
booking.getStatus() == Booking.BookingStatus.Pending 
    && "Paid".equals(booking.getPaymentStatus())
```

### 2. Hoàn tiền tự động
- Khi Owner từ chối, hệ thống **TỰ ĐỘNG** tạo payment record với type=Refund
- Không cần Staff thao tác thủ công
- Customer nhận thông báo ngay lập tức

### 3. Thông báo realtime
- Owner nhận thông báo khi có booking mới (sau thanh toán)
- Customer nhận thông báo khi Owner duyệt/từ chối
- Staff nhận thông báo khi cần hoàn tiền (nếu cần xử lý thủ công)

---

## 🔧 Files liên quan

### Controllers
- `PaymentMethodController.java` - Xử lý thanh toán VNPay
- `OwnerBookingController.java` - Owner duyệt/từ chối booking

### Models
- `Booking.java` - Model booking với Status enum
- `Payment.java` - Model payment với PaymentStatus enum

### Database
- `sql2.sql` - Schema database
- `add-pending-approval-status.sql` - Query kiểm tra booking chờ duyệt

---

## 🚀 Bước tiếp theo (TODO)

1. ✅ Tạo view HTML cho Owner
   - `owner/pending-bookings.html` - Danh sách booking chờ duyệt
   - `owner/booking-detail.html` - Chi tiết booking

2. ⏳ Tích hợp thông báo realtime
   - WebSocket cho thông báo realtime
   - Email notification

3. ⏳ Xử lý hoàn tiền thực tế
   - Tích hợp VNPay Refund API (nếu có)
   - Hoặc xử lý thủ công qua Staff dashboard

4. ⏳ Báo cáo và thống kê
   - Dashboard cho Owner
   - Thống kê booking theo trạng thái
   - Báo cáo doanh thu

---

## 📝 Ghi chú

- **KHÔNG CẦN** thêm trạng thái mới vào database
- Sử dụng kết hợp `Status` và `PaymentStatus` để phân biệt các trường hợp
- Logic rõ ràng, dễ maintain và mở rộng
