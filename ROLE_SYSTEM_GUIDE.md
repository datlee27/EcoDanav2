# Hệ Thống Phân Quyền EvoDana

## Tổng Quan

Hệ thống phân quyền đã được implement hoàn chỉnh với 3 role chính:
- **Customer**: Khách hàng thông thường
- **Staff**: Nhân viên quản lý
- **Admin**: Quản trị viên

## Cấu Trúc Database

### Bảng Roles
```sql
CREATE TABLE `Roles` (
  `RoleId` char(36) NOT NULL,
  `RoleName` varchar(50) DEFAULT NULL,
  `NormalizedName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`RoleId`)
);
```

### Dữ Liệu Mẫu
```sql
INSERT INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('customer-role-id', 'Customer', 'CUSTOMER'),
('staff-role-id', 'Staff', 'STAFF'),
('admin-role-id', 'Admin', 'ADMIN');
```

## Cách Sử Dụng

### 1. Tạo User Test
Truy cập: `http://localhost:8080/test/create-users`
- Tạo 3 user test với các role khác nhau
- Email: customer@test.com, staff@test.com, admin@test.com
- Password: password123

### 2. Login Test Nhanh
- Customer: `http://localhost:8080/test/login?role=customer`
- Staff: `http://localhost:8080/test/login?role=staff`
- Admin: `http://localhost:8080/test/login?role=admin`

### 3. Login Thông Thường
- Truy cập: `http://localhost:8080/login`
- Sử dụng email/password của user test

## Luồng Hoạt Động

### 1. Đăng Nhập
1. User đăng nhập qua `/login`
2. `AuthController` xác thực thông tin
3. Load user với role đầy đủ từ database
4. Lưu user vào session
5. Redirect đến `/dashboard`

### 2. Dashboard Routing
1. `DashboardRouterController` kiểm tra role của user
2. Redirect đến dashboard phù hợp:
   - **Customer** → `/dashboard` (Customer Dashboard)
   - **Staff** → `/owner` (Management Panel)
   - **Admin** → `/admin` (Admin Panel)

### 3. Kiểm Tra Quyền
- Mỗi controller kiểm tra role trước khi cho phép truy cập
- Sử dụng `UserService.isAdmin()`, `UserService.isStaff()`, `UserService.isCustomer()`

## Các File Đã Tạo/Cập Nhật

### 1. Repository
- `RoleRepository.java` - Truy vấn roles từ database

### 2. Service
- `RoleService.java` - Logic xử lý roles
- `UserService.java` - Cập nhật để load user với role

### 3. Controller
- `DashboardRouterController.java` - Routing dựa trên role
- `TestController.java` - Tạo user test và login nhanh
- `HomeController.java` - Redirect dựa trên role
- `AuthController.java` - Cập nhật login flow
- `DashboardController.java` - Cập nhật kiểm tra quyền

### 4. Template
- `nav.html` - Hiển thị menu dựa trên role
- `dashboard.html` - Hiển thị thông tin role

## API Endpoints

### Authentication
- `GET /login` - Trang đăng nhập
- `POST /login` - Xử lý đăng nhập
- `GET /logout` - Đăng xuất

### Dashboard
- `GET /dashboard` - Dashboard chính (redirect dựa trên role)
- `GET /dashboard/customer` - Customer dashboard
- `GET /dashboard/staff` - Staff dashboard  
- `GET /dashboard/admin` - Admin dashboard

### Role-specific
- `GET /owner` - Management panel (Staff/Admin)
- `GET /admin` - Admin panel (Admin only)

### Test
- `GET /test/create-users` - Tạo user test
- `GET /test/login?role={role}` - Login test nhanh

## Bảo Mật

### 1. Session Management
- User được lưu trong session sau khi đăng nhập
- Mỗi request kiểm tra session và role

### 2. Role Validation
- Kiểm tra role trước khi truy cập các endpoint
- Redirect về login nếu không có quyền

### 3. Database Security
- Role được load từ database, không hardcode
- Foreign key constraints đảm bảo tính toàn vẹn

## Mở Rộng

### Thêm Role Mới
1. Thêm role vào database
2. Cập nhật `RoleService` với role ID mới
3. Thêm logic routing trong `DashboardRouterController`
4. Cập nhật templates để hiển thị role mới

### Thêm Quyền Chi Tiết
1. Tạo bảng `Permissions`
2. Tạo bảng `RolePermissions` (many-to-many)
3. Cập nhật `UserService` để kiểm tra permission
4. Thêm annotation `@PreAuthorize` cho các endpoint

## Troubleshooting

### 1. Role không load được
- Kiểm tra database có dữ liệu roles chưa
- Chạy `insert_sample_data.sql`
- Kiểm tra JPA mapping trong `User.java`

### 2. Redirect không đúng
- Kiểm tra `DashboardRouterController`
- Kiểm tra role name trong database
- Kiểm tra logic trong `UserService`

### 3. Session mất
- Kiểm tra session timeout
- Kiểm tra cookie settings
- Kiểm tra security configuration
