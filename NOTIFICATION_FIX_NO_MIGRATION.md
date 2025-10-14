# ✅ Fix Notification Error - Không cần thêm cột database

## 🎯 Giải pháp

Thay vì thêm cột vào database, tôi đã sửa **Model Java** để phù hợp với cấu trúc database hiện tại.

---

## 📊 Database hiện tại

Bảng `Notification` chỉ có **5 cột**:
- `NotificationId` (char 36)
- `UserId` (char 36)
- `Message` (text)
- `CreatedDate` (datetime)
- `IsRead` (tinyint)

---

## 🔧 Đã sửa

### File: `Notification.java`

**Thay đổi:**
```java
// TỪ:
@Column(name = "RelatedId", length = 36)
private String relatedId;

@Column(name = "NotificationType", length = 50)
private String notificationType;

// THÀNH:
@Transient  // Không map vào database
private String relatedId;

@Transient  // Không map vào database
private String notificationType;
```

**Giải thích:**
- `@Transient` = field này chỉ tồn tại trong Java, không map vào database
- Các field này vẫn có thể dùng trong code (getter/setter)
- Nhưng Hibernate sẽ KHÔNG tìm chúng trong database

---

## ✅ Kết quả

### Trước khi sửa:
```
❌ Error: Unknown column 'n1_0.NotificationType' in 'field list'
```

### Sau khi sửa:
```
✅ Notification hoạt động bình thường
✅ Không cần migration database
✅ Dữ liệu cũ vẫn giữ nguyên
```

---

## 🔄 Cách hoạt động

### 1. Lưu notification vào database:
```java
Notification notif = new Notification();
notif.setUserId("user123");
notif.setMessage("Có đơn đặt xe mới");
notif.setNotificationType("BOOKING");  // Chỉ dùng trong code
notif.setRelatedId("booking123");       // Chỉ dùng trong code
notificationRepository.save(notif);
```

**Kết quả trong database:**
```
NotificationId | UserId  | Message              | CreatedDate | IsRead
abc-123        | user123 | Có đơn đặt xe mới    | 2025-10-14  | 0
```
→ `notificationType` và `relatedId` KHÔNG được lưu vào database

### 2. Đọc notification từ database:
```java
List<Notification> notifications = notificationRepository.findAll();
// notifications[0].getNotificationType() = null
// notifications[0].getRelatedId() = null
```
→ Các field `@Transient` sẽ là `null` khi đọc từ database

### 3. Sử dụng trong code:
```java
// Có thể set giá trị tạm thời trong code
notification.setNotificationType("REFUND");
notification.setRelatedId("booking123");

// Nhưng khi save, 2 field này sẽ KHÔNG được lưu vào database
```

---

## 🎨 Cách hiển thị notification type trong UI

Vì `notificationType` không được lưu trong database, ta có thể:

### Cách 1: Parse từ message
```java
// Trong view hoặc service
public String getNotificationType(Notification notification) {
    String message = notification.getMessage();
    if (message.contains("hoàn tiền")) return "REFUND";
    if (message.contains("đặt xe")) return "BOOKING";
    if (message.contains("thanh toán")) return "PAYMENT";
    return "GENERAL";
}
```

### Cách 2: Thêm prefix vào message
```java
// Khi tạo notification
String message = "[REFUND] Cần xử lý hoàn tiền cho đơn BK123";
notification.setMessage(message);
```

Trong view:
```html
<div th:if="${notification.message.startsWith('[REFUND]')}">
    <i class="fas fa-money-bill-wave text-red-600"></i>
</div>
```

### Cách 3: Tạo enum trong message (JSON)
```java
// Lưu message dạng JSON
String message = "{\"type\":\"REFUND\",\"text\":\"Cần xử lý hoàn tiền\",\"bookingId\":\"BK123\"}";
notification.setMessage(message);
```

---

## 🚀 Restart và test

1. **Restart ứng dụng Spring Boot**
2. **Vào `/staff/notifications`**
3. **Kiểm tra**: Không còn lỗi SQL nữa!

---

## 📝 Tóm tắt

| Trước | Sau |
|-------|-----|
| Model có field map vào database | Model có field `@Transient` |
| Database thiếu cột → Lỗi | Database không cần cột → OK |
| Cần migration SQL | Không cần migration |
| Dữ liệu cũ bị ảnh hưởng | Dữ liệu cũ giữ nguyên |

---

## ⚠️ Lưu ý

**Ưu điểm:**
- ✅ Không cần sửa database
- ✅ Không ảnh hưởng dữ liệu cũ
- ✅ Fix nhanh, đơn giản

**Nhược điểm:**
- ❌ Không lưu được `notificationType` và `relatedId` vào database
- ❌ Không query được theo type
- ❌ Phải parse từ message để biết type

**Khuyến nghị:**
- Nếu cần query theo type → Nên thêm cột vào database
- Nếu chỉ hiển thị → Giải pháp này OK

---

## 🔄 Nếu muốn thêm cột sau này

Chạy migration:
```sql
ALTER TABLE Notification 
ADD COLUMN NotificationType VARCHAR(50) NULL AFTER Message;

ALTER TABLE Notification 
ADD COLUMN RelatedId VARCHAR(36) NULL AFTER NotificationType;
```

Và sửa lại Model:
```java
// Xóa @Transient, thêm @Column
@Column(name = "NotificationType", length = 50)
private String notificationType;

@Column(name = "RelatedId", length = 36)
private String relatedId;
```

---

**✅ Đã fix xong! Restart app và test ngay!**
