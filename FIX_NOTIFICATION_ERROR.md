# 🔧 Fix Notification Error - Database Migration

## ❌ Lỗi

```
Unknown column 'n1_0.NotificationType' in 'field list'
```

**Nguyên nhân**: Bảng `Notification` trong database thiếu 2 cột:
- `NotificationType`
- `RelatedId`

---

## ✅ Giải pháp

### Cách 1: Chạy Migration SQL (Khuyến nghị)

**Bước 1**: Mở MySQL Workbench hoặc command line

**Bước 2**: Chạy script sau:

```sql
USE ecodanav2;

-- Thêm cột NotificationType
ALTER TABLE Notification 
ADD COLUMN NotificationType VARCHAR(50) NULL AFTER Message;

-- Thêm cột RelatedId
ALTER TABLE Notification 
ADD COLUMN RelatedId VARCHAR(36) NULL AFTER NotificationType;

-- Kiểm tra kết quả
DESCRIBE Notification;

-- Cập nhật dữ liệu cũ (nếu có)
UPDATE Notification 
SET NotificationType = 'GENERAL' 
WHERE NotificationType IS NULL;
```

**Hoặc chạy file SQL**:
```bash
mysql -u root -p ecodanav2 < src/main/resources/db/add-notification-columns.sql
```

---

### Cách 2: Chạy từ Command Line

```bash
# Windows
mysql -u root -p

# Sau khi đăng nhập MySQL:
USE ecodanav2;
ALTER TABLE Notification ADD COLUMN NotificationType VARCHAR(50) NULL AFTER Message;
ALTER TABLE Notification ADD COLUMN RelatedId VARCHAR(36) NULL AFTER NotificationType;
DESCRIBE Notification;
```

---

### Cách 3: Drop và Recreate bảng (Nếu không có dữ liệu quan trọng)

```sql
USE ecodanav2;

-- Backup dữ liệu cũ (nếu cần)
CREATE TABLE Notification_backup AS SELECT * FROM Notification;

-- Drop bảng cũ
DROP TABLE IF EXISTS Notification;

-- Tạo lại bảng với đầy đủ cột
CREATE TABLE `Notification` (
    `NotificationId` char(36) NOT NULL,
    `UserId` char(36) NOT NULL,
    `Message` text NOT NULL,
    `NotificationType` varchar(50) DEFAULT 'GENERAL',
    `RelatedId` varchar(36) DEFAULT NULL,
    `CreatedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `IsRead` tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`NotificationId`),
    KEY `UserId` (`UserId`),
    CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `Users` (`UserId`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

---

## 🔍 Kiểm tra sau khi fix

```sql
-- Xem cấu trúc bảng
DESCRIBE Notification;

-- Kết quả mong đợi:
-- NotificationId    | char(36)      | NO  | PRI | NULL    |
-- UserId            | char(36)      | NO  | MUL | NULL    |
-- Message           | text          | NO  |     | NULL    |
-- NotificationType  | varchar(50)   | YES |     | NULL    |  <-- CỘT MỚI
-- RelatedId         | varchar(36)   | YES |     | NULL    |  <-- CỘT MỚI
-- CreatedDate       | datetime      | NO  |     | CURRENT_TIMESTAMP |
-- IsRead            | tinyint(1)    | NO  |     | 0       |
```

---

## 🚀 Sau khi fix

1. **Restart ứng dụng Spring Boot**
2. **Test lại**: Vào `/staff/notifications`
3. **Kiểm tra**: Không còn lỗi nữa

---

## 📝 Giải thích

Model `Notification.java` có các fields:
```java
private String notificationType;  // Cần cột NotificationType
private String relatedId;          // Cần cột RelatedId
```

Nhưng bảng database chưa có 2 cột này → Lỗi SQL.

Sau khi thêm 2 cột, Hibernate sẽ map được đúng và không còn lỗi.

---

## ⚠️ Lưu ý

- Nếu có dữ liệu notification cũ, chúng sẽ có `NotificationType = NULL`
- Script đã cập nhật tất cả NULL thành 'GENERAL'
- Các notification mới sẽ có type: 'REFUND', 'BOOKING', 'PAYMENT', etc.

---

## 🎯 Quick Fix Command

Copy-paste vào MySQL:

```sql
USE ecodanav2;
ALTER TABLE Notification ADD COLUMN NotificationType VARCHAR(50) NULL AFTER Message;
ALTER TABLE Notification ADD COLUMN RelatedId VARCHAR(36) NULL AFTER NotificationType;
UPDATE Notification SET NotificationType = 'GENERAL' WHERE NotificationType IS NULL;
```

**Done! Restart app và test lại!** ✅
