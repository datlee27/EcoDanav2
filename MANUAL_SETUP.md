# Hướng dẫn setup database thủ công

## Vấn đề hiện tại:
- Lỗi: `Cannot add or update a child row: a foreign key constraint fails`
- Nguyên nhân: Bảng `Roles` chưa có dữ liệu, nên không thể tạo user với `RoleId`

## Cách sửa:

### Bước 1: Mở MySQL Workbench hoặc MySQL Command Line

### Bước 2: Chạy lệnh SQL sau:

```sql
USE ecodana;

-- Insert roles if they don't exist
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('customer-role-id', 'Customer', 'CUSTOMER'),
('staff-role-id', 'Staff', 'STAFF'),
('admin-role-id', 'Admin', 'ADMIN');
```

### Bước 3: Kiểm tra dữ liệu đã được tạo:

```sql
SELECT * FROM Roles;
```

### Bước 4: Chạy lại ứng dụng:

```bash
mvn spring-boot:run
```

### Bước 5: Test đăng ký:

- Truy cập: http://localhost:8080/register
- Điền thông tin: Email, Số điện thoại, Mật khẩu
- Nhấn "Đăng Ký"

## Kết quả mong đợi:
- Đăng ký thành công
- Chuyển về trang home với thông báo "Đăng ký thành công!"
- Có thể đăng nhập bằng tài khoản vừa tạo
