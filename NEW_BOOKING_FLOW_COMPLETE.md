# 🔄 LUỒNG ĐẶT XE MỚI - Customer → Staff → Owner

## 📋 Luồng nghiệp vụ ĐÚNG

```
┌─────────────────────────────────────────────────────────────────┐
│  1. CUSTOMER: Đặt xe và Thanh toán                              │
│     - Chọn xe → Điền thông tin → Thanh toán VNPay              │
│     - VNPay return: Payment success                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  2. SYSTEM: Cập nhật trạng thái                                 │
│     - Payment.status = "Completed"                              │
│     - Booking.paymentStatus = "Paid"                            │
│     - Booking.status = "Pending"                                │
│     - Gửi notification cho STAFF ⭐                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  3. STAFF: Xác nhận đã nhận tiền                                │
│     URL: /staff/payment-confirmation                            │
│     - Xem danh sách booking đã thanh toán                       │
│     - Kiểm tra thông tin payment                                │
│     - Click "Xác nhận đã nhận tiền"                             │
│     - Booking.handledBy = Staff (đánh dấu Staff đã xử lý)      │
│     - Gửi notification cho OWNER ⭐                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  4. OWNER: Duyệt hoặc Từ chối                                   │
│     URL: /owner/bookings/pending-approval                       │
│     - Xem booking đã được Staff xác nhận                        │
│     - Kiểm tra thông tin xe, lịch trình                         │
│                                                                  │
│     A. NẾU DUYỆT:                                               │
│        - Booking.status = "Approved"                            │
│        - Gửi notification cho Customer                          │
│        - Customer nhận xe theo lịch                             │
│                                                                  │
│     B. NẾU TỪ CHỐI:                                             │
│        - Booking.status = "Cancelled"                           │
│        - Booking.paymentStatus = "Refunded"                     │
│        - Tạo Payment Refund record                              │
│        - Gửi notification cho Customer                          │
│        - Gửi notification cho Staff (xử lý hoàn tiền) ⭐       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Vai trò của từng bên

### 👤 CUSTOMER
- **Nhiệm vụ**: Đặt xe và thanh toán
- **Không có quyền**: Hủy sau khi thanh toán (phải chờ Owner từ chối)

### 👨‍💼 STAFF
- **Nhiệm vụ**: Xác nhận đã nhận tiền từ VNPay
- **KHÔNG có quyền**: Duyệt/Từ chối booking
- **Chỉ làm**: Kiểm tra payment → Xác nhận → Chuyển cho Owner

### 👔 OWNER
- **Nhiệm vụ**: Quyết định cuối cùng về booking
- **Có quyền**: Duyệt hoặc Từ chối
- **Chỉ nhận**: Booking đã được Staff xác nhận payment

---

## 🔧 Các file đã tạo/sửa

### Backend Controllers

#### 1. **PaymentMethodController.java** ✅
```java
// Sau khi VNPay return success
booking.setPaymentStatus("Paid");
booking.setStatus(Booking.BookingStatus.Pending);
bookingService.updateBooking(booking);

// Gửi notification cho Staff
notificationService.createNotificationForAllStaff(
    "Đơn đặt xe " + booking.getBookingCode() + " đã thanh toán thành công. Vui lòng xác nhận đã nhận tiền.",
    booking.getBookingId(),
    "PAYMENT_CONFIRMATION"
);
```

#### 2. **StaffPaymentConfirmationController.java** ✅ (MỚI)
- `GET /staff/payment-confirmation` - Xem danh sách chờ xác nhận
- `POST /staff/payment-confirmation/confirm/{bookingId}` - Xác nhận đã nhận tiền
- `GET /staff/payment-confirmation/detail/{bookingId}` - Xem chi tiết

```java
// Khi Staff xác nhận
booking.setHandledBy(staff);
bookingService.updateBooking(booking);

// Gửi notification cho Owner
notificationService.createNotificationForAllAdmins(
    "Đơn đặt xe " + booking.getBookingCode() + " đã được Staff xác nhận thanh toán. Vui lòng duyệt đơn.",
    booking.getBookingId(),
    "BOOKING_APPROVAL"
);
```

#### 3. **OwnerBookingController.java** ✅ (ĐÃ SỬA)
```java
// Chỉ lấy booking đã được Staff xác nhận
List<Booking> pendingBookings = bookingService.getAllBookings().stream()
    .filter(b -> b.getStatus() == Booking.BookingStatus.Pending && 
                "Paid".equals(b.getPaymentStatus()) &&
                b.getHandledBy() != null) // ⭐ Đã được Staff xác nhận
    .collect(Collectors.toList());
```

### Frontend Views

#### 1. **staff/payment-confirmation.html** ✅ (MỚI)
- Hiển thị danh sách booking đã thanh toán
- Thông tin Customer, Vehicle, Payment
- Button "Xác nhận đã nhận tiền"
- Responsive, modern UI

#### 2. **staff/staff-dashboard.html** ✅ (ĐÃ SỬA)
- Thêm Quick Action: "Xác nhận thanh toán"
- Link: `/staff/payment-confirmation`

#### 3. **owner/pending-bookings.html** ✅ (ĐÃ CÓ)
- Hiển thị booking đã được Staff xác nhận
- Owner duyệt/từ chối

---

## 📊 Database Schema

### Booking Table
```sql
BookingId           CHAR(36)
Status              ENUM('Pending', 'Approved', 'Rejected', 'Ongoing', 'Completed', 'Cancelled')
PaymentStatus       VARCHAR(50)  -- 'Unpaid', 'Paid', 'Refunded'
HandledBy           CHAR(36)     -- ⭐ Staff ID (NULL = chưa xác nhận, NOT NULL = đã xác nhận)
```

### Notification Table
```sql
NotificationId      CHAR(36)
UserId              CHAR(36)
Message             TEXT
CreatedDate         DATETIME
IsRead              TINYINT(1)
```

---

## 🔄 Trạng thái Booking qua các bước

| Bước | Status | PaymentStatus | HandledBy | Ý nghĩa |
|------|--------|---------------|-----------|---------|
| 1. Customer đặt xe | `Pending` | `Unpaid` | `NULL` | Chờ thanh toán |
| 2. Customer thanh toán | `Pending` | `Paid` | `NULL` | Chờ Staff xác nhận |
| 3. Staff xác nhận | `Pending` | `Paid` | `Staff ID` | Chờ Owner duyệt |
| 4a. Owner duyệt | `Approved` | `Paid` | `Staff ID` | Đã duyệt |
| 4b. Owner từ chối | `Cancelled` | `Refunded` | `Staff ID` | Đã hủy + hoàn tiền |

---

## 🎨 UI Flow

### Staff Dashboard
```
┌─────────────────────────────────────────┐
│  Staff Dashboard                         │
│  ┌─────────────────────────────────┐   │
│  │ Quick Actions                    │   │
│  │ ┌─────────────────────────────┐ │   │
│  │ │ 💰 Xác nhận thanh toán      │ │   │
│  │ │    Xác nhận đã nhận tiền    │ │   │
│  │ └─────────────────────────────┘ │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                ↓ Click
┌─────────────────────────────────────────┐
│  Xác nhận thanh toán                     │
│  ┌─────────────────────────────────┐   │
│  │ Đơn BK123456 - ĐÃ THANH TOÁN   │   │
│  │ Khách: Nguyễn Văn A             │   │
│  │ Xe: VinFast VF8                 │   │
│  │ Số tiền: 1.000.000 ₫            │   │
│  │ [Xác nhận đã nhận tiền]         │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### Owner Dashboard
```
┌─────────────────────────────────────────┐
│  Owner Dashboard                         │
│  ┌─────────────────────────────────┐   │
│  │ Đơn chờ duyệt (3)               │   │
│  │ ┌─────────────────────────────┐ │   │
│  │ │ BK123456 - ĐÃ XÁC NHẬN      │ │   │
│  │ │ Staff: Trần Thị B đã xác nhận│ │   │
│  │ │ Khách: Nguyễn Văn A          │ │   │
│  │ │ [Duyệt] [Từ chối]            │ │   │
│  │ └─────────────────────────────┘ │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 🚀 Testing Flow

### Test Case 1: Happy Path (Duyệt thành công)

1. **Customer**: Đặt xe BK123456 → Thanh toán VNPay → Success
2. **Staff**: 
   - Vào `/staff/payment-confirmation`
   - Thấy BK123456 trong danh sách
   - Click "Xác nhận đã nhận tiền"
   - ✅ Success message
3. **Owner**:
   - Vào `/owner/bookings/pending-approval`
   - Thấy BK123456 (đã có Staff xác nhận)
   - Click "Duyệt"
   - ✅ Booking.status = Approved
4. **Customer**: Nhận notification "Đơn đã được duyệt"

### Test Case 2: Reject Path (Từ chối + Hoàn tiền)

1. **Customer**: Đặt xe BK789 → Thanh toán → Success
2. **Staff**: Xác nhận đã nhận tiền
3. **Owner**: 
   - Vào `/owner/bookings/pending-approval`
   - Thấy BK789
   - Click "Từ chối" + Nhập lý do
   - ✅ Booking.status = Cancelled
   - ✅ PaymentStatus = Refunded
   - ✅ Tạo Payment Refund
4. **Staff**: Nhận notification "Cần xử lý hoàn tiền cho BK789"
5. **Customer**: Nhận notification "Đơn bị từ chối, tiền đã hoàn"

---

## ⚠️ Lưu ý quan trọng

### 1. Staff KHÔNG được duyệt booking
```java
// ❌ SAI - Staff không có quyền này
booking.setStatus(Booking.BookingStatus.Approved);

// ✅ ĐÚNG - Staff chỉ xác nhận payment
booking.setHandledBy(staff);
```

### 2. Owner chỉ nhận booking đã xác nhận
```java
// ✅ ĐÚNG - Filter có HandledBy
.filter(b -> b.getHandledBy() != null)

// ❌ SAI - Không filter
.filter(b -> b.getStatus() == Booking.BookingStatus.Pending)
```

### 3. Notification flow
```
Payment Success → Staff
Staff Confirm   → Owner
Owner Approve   → Customer
Owner Reject    → Customer + Staff
```

---

## 📝 Checklist hoàn thành

### Backend
- ✅ PaymentMethodController: Gửi notification cho Staff
- ✅ StaffPaymentConfirmationController: Xác nhận payment
- ✅ OwnerBookingController: Filter booking đã xác nhận
- ✅ NotificationService: createNotificationForAllStaff()
- ✅ NotificationService: createNotificationForAllAdmins()

### Frontend
- ✅ staff/payment-confirmation.html: Trang xác nhận
- ✅ staff/staff-dashboard.html: Thêm Quick Action
- ✅ owner/pending-bookings.html: Hiển thị Staff info

### Database
- ✅ Booking.handledBy: Đánh dấu Staff đã xác nhận
- ✅ Notification table: Lưu thông báo

---

## 🎯 Kết luận

**Luồng mới ĐÚNG:**
```
Customer Pay → Staff Confirm → Owner Approve/Reject
```

**Staff chỉ làm:**
- ✅ Xác nhận đã nhận tiền
- ✅ Chuyển request cho Owner
- ❌ KHÔNG duyệt booking

**Owner có quyền:**
- ✅ Duyệt booking
- ✅ Từ chối booking + Hoàn tiền

**Hệ thống đã sẵn sàng test!** 🚀
