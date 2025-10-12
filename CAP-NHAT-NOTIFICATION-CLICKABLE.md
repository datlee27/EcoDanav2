# CẬP NHẬT: NOTIFICATION CÓ THỂ CLICK

## ✅ Đã thêm tính năng

### **Click notification → Chuyển đến trang quản lý tương ứng**

Khi click vào thông báo:
1. ✅ Đánh dấu đã đọc
2. ✅ Đóng dropdown
3. ✅ Chuyển đến tab tương ứng:
   - **BOOKING** → Tab "Booking Management"
   - **PAYMENT** → Tab "Payment Management"
   - **CONTRACT** → Tab "Contract Management"

## 🔧 Thay đổi kỹ thuật

### 1. **Model Notification**
Thêm 2 trường mới:
- `RelatedId` (VARCHAR 36) - ID của booking/payment/contract
- `NotificationType` (VARCHAR 50) - Loại: "BOOKING", "PAYMENT", "CONTRACT"

### 2. **Service**
Thêm method mới:
```java
createNotificationForAllAdmins(message, relatedId, notificationType)
```

### 3. **BookingController**
Khi tạo booking → Tạo notification với:
- Message: Thông tin đơn đặt xe
- RelatedId: `booking.getBookingId()`
- NotificationType: `"BOOKING"`

### 4. **Frontend JavaScript**
Function `handleNotificationClick()`:
- Đánh dấu đã đọc
- Đóng dropdown
- Switch tab dựa trên `notificationType`

## 🚀 Cách sử dụng

### **Bước 1: Cập nhật database**
```sql
-- Chạy lại file: create-notification-table.sql
-- Hoặc chạy ALTER TABLE nếu bảng đã tồn tại
```

### **Bước 2: Restart ứng dụng**
```bash
mvn spring-boot:run
```

### **Bước 3: Test**

**Scenario:**
1. Khách hàng đặt xe
2. Admin nhận thông báo (badge đỏ)
3. **Click vào thông báo**
4. → Tự động chuyển đến tab "Booking Management"
5. → Thông báo được đánh dấu đã đọc
6. → Badge giảm 1

## 📱 Demo

### **Trước khi click:**
```
┌────────────────────────────────────────┐
│ Thông báo                              │
├────────────────────────────────────────┤
│ 🚗 Đơn đặt xe mới #BK1234567890      │
│    Khách hàng: john_doe                │
│    Xe: VinFast VF 6 Plus               │
│    2 phút trước                     •  │ ← Chưa đọc
└────────────────────────────────────────┘
```

### **Sau khi click:**
```
1. Dropdown đóng
2. Chuyển sang tab "Booking Management"
3. Badge cập nhật (giảm 1)
4. Thông báo đã đọc (nền trắng)
```

## 🎯 Luồng hoạt động

```
User clicks notification
    ↓
handleNotificationClick(id, relatedId, type)
    ↓
Mark as read (API call)
    ↓
Close dropdown
    ↓
Switch tab based on type:
    - BOOKING → switchTab('bookings')
    - PAYMENT → switchTab('payments')
    - CONTRACT → switchTab('contracts')
    ↓
Update badge count
```

## 💡 Mở rộng trong tương lai

### **Highlight specific item:**
```javascript
if (notificationType === 'BOOKING' && relatedId) {
    switchTab('bookings');
    // Highlight the specific booking
    setTimeout(() => {
        const bookingRow = document.querySelector(`[data-booking-id="${relatedId}"]`);
        if (bookingRow) {
            bookingRow.scrollIntoView({ behavior: 'smooth' });
            bookingRow.classList.add('highlight-animation');
        }
    }, 500);
}
```

### **Direct link to detail page:**
```javascript
if (notificationType === 'BOOKING' && relatedId) {
    window.location.href = `/admin/bookings/${relatedId}`;
}
```

### **Filter by booking ID:**
```javascript
if (notificationType === 'BOOKING' && relatedId) {
    switchTab('bookings');
    // Apply filter
    const searchInput = document.querySelector('#bookingSearch');
    if (searchInput) {
        searchInput.value = relatedId;
        searchInput.dispatchEvent(new Event('input'));
    }
}
```

## 📊 Notification Types

| Type | Description | Navigate To |
|------|-------------|-------------|
| BOOKING | Đơn đặt xe mới | Booking Management |
| PAYMENT | Thanh toán mới | Payment Management |
| CONTRACT | Hợp đồng mới | Contract Management |
| VEHICLE | Xe mới/cập nhật | Vehicle Management |
| USER | User mới/cập nhật | User Management |

## ✅ Checklist

- [x] Thêm RelatedId và NotificationType vào Model
- [x] Cập nhật Service với method mới
- [x] Cập nhật BookingController truyền bookingId
- [x] Cập nhật API trả về relatedId và type
- [x] Thêm handleNotificationClick() function
- [x] Cập nhật SQL schema
- [x] Test đầy đủ

**Notification giờ đã có thể click và navigate!** 🎉
