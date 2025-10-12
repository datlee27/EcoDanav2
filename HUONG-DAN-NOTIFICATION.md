# HƯỚNG DẪN HỆ THỐNG THÔNG BÁO ADMIN

## ✅ Đã hoàn thành

### 1. **Database**
- ✅ Bảng `Notification` với các trường:
  - NotificationId (PK)
  - UserId (FK → User)
  - Message (TEXT)
  - CreatedDate (DATETIME)
  - IsRead (BOOLEAN)

### 2. **Backend**
- ✅ Model: `Notification.java`
- ✅ Repository: `NotificationRepository.java`
- ✅ Service: `NotificationService.java`
- ✅ API Controller: `NotificationApiController.java`

### 3. **Tự động tạo thông báo**
- ✅ Khi khách hàng đặt xe → Tạo notification cho TẤT CẢ admin
- ✅ Nội dung: "Đơn đặt xe mới #BK123 - Khách hàng: username - Xe: VinFast VF 6 Plus - Tổng: 4,510,401 ₫"

### 4. **Frontend - Admin Dashboard**
- ✅ Icon chuông ở topbar
- ✅ Badge đỏ hiển thị số thông báo chưa đọc
- ✅ Dropdown danh sách thông báo
- ✅ Highlight thông báo chưa đọc (nền xanh nhạt)
- ✅ Click để đánh dấu đã đọc
- ✅ Nút "Đánh dấu tất cả đã đọc"
- ✅ Tự động refresh mỗi 30 giây

## 🚀 Cách sử dụng

### **Bước 1: Chạy SQL tạo bảng**
```sql
-- Chạy file: create-notification-table.sql
```

### **Bước 2: Restart ứng dụng**
```bash
mvn spring-boot:run
```

### **Bước 3: Test**
1. **Khách hàng đặt xe:**
   - Đăng nhập tài khoản customer
   - Chọn xe → Đặt xe → Xác nhận

2. **Admin nhận thông báo:**
   - Đăng nhập tài khoản admin
   - Thấy badge đỏ trên icon chuông (số 1)
   - Click chuông → Xem thông báo
   - Click vào thông báo → Đánh dấu đã đọc

## 📱 Giao diện

### **Icon chuông (Topbar)**
```
┌─────────────────────────────────┐
│  [☰]  EvoDana Admin      [🔔¹]  │  ← Badge đỏ số 1
└─────────────────────────────────┘
```

### **Dropdown thông báo**
```
┌───────────────────────────────────────┐
│ Thông báo    [Đánh dấu tất cả đã đọc] │
├───────────────────────────────────────┤
│ 🚗 Đơn đặt xe mới #BK1234567890      │
│    Khách hàng: john_doe               │
│    Xe: VinFast VF 6 Plus              │
│    Tổng: 4,510,401 ₫                  │
│    2 phút trước                    •  │ ← Chấm xanh = chưa đọc
├───────────────────────────────────────┤
│ 🚗 Đơn đặt xe mới #BK1234567889      │
│    Khách hàng: jane_smith             │
│    Xe: Mitsubishi Xpander             │
│    Tổng: 3,410,401 ₫                  │
│    1 giờ trước                        │ ← Đã đọc
└───────────────────────────────────────┘
```

## 🎯 Tính năng

### **1. Real-time Badge**
- Badge hiển thị số thông báo chưa đọc
- Tự động cập nhật mỗi 30 giây
- Ẩn khi không có thông báo mới

### **2. Dropdown thông minh**
- Click chuông → Mở dropdown
- Click bên ngoài → Đóng dropdown
- Tự động load khi mở

### **3. Đánh dấu đã đọc**
- Click vào thông báo → Đánh dấu đã đọc
- Nền xanh nhạt → Nền trắng
- Chấm xanh biến mất
- Badge giảm 1

### **4. Đánh dấu tất cả**
- Click "Đánh dấu tất cả đã đọc"
- Tất cả thông báo → Đã đọc
- Badge về 0

### **5. Hiển thị thời gian**
- "Vừa xong" (< 1 phút)
- "5 phút trước"
- "2 giờ trước"
- "3 ngày trước"
- "12/11/2025" (> 7 ngày)

## 🔧 API Endpoints

### **GET /api/notifications**
Lấy tất cả thông báo của user hiện tại
```json
{
  "notifications": [...],
  "unreadCount": 5
}
```

### **GET /api/notifications/unread-count**
Lấy số lượng thông báo chưa đọc
```json
{
  "unreadCount": 5
}
```

### **POST /api/notifications/{id}/mark-read**
Đánh dấu 1 thông báo đã đọc

### **POST /api/notifications/mark-all-read**
Đánh dấu tất cả đã đọc

### **DELETE /api/notifications/{id}**
Xóa thông báo

## 💡 Mở rộng

Có thể thêm:
- ✨ Notification cho các sự kiện khác (payment, contract, etc.)
- ✨ Push notification (WebSocket)
- ✨ Email notification
- ✨ SMS notification
- ✨ Notification settings (bật/tắt từng loại)
- ✨ Notification history (xem lại đã xóa)

## 🎨 Customization

### **Thay đổi thời gian refresh:**
```javascript
// Trong admin-dashboard.html, dòng 784
setInterval(loadNotificationCount, 30000); // 30 giây
// Đổi thành 60000 cho 1 phút
```

### **Thay đổi nội dung thông báo:**
```java
// Trong BookingController.java, dòng 221-227
String notificationMessage = String.format(
    "Đơn đặt xe mới #%s - ...",
    // Tùy chỉnh format ở đây
);
```

### **Thêm notification cho sự kiện khác:**
```java
// Ví dụ: Khi payment thành công
notificationService.createNotificationForAllAdmins(
    "💰 Thanh toán mới: " + amount + " ₫ - Booking #" + bookingCode
);
```

## ✅ Checklist triển khai

- [x] Tạo bảng Notification
- [x] Tạo Model, Repository, Service
- [x] Tạo API Controller
- [x] Thêm notification bell vào admin dashboard
- [x] Thêm JavaScript xử lý
- [x] Tự động tạo notification khi booking
- [x] Test đầy đủ

**Hệ thống notification đã sẵn sàng!** 🎉
