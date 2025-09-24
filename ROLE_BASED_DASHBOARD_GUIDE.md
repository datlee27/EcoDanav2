# Hướng Dẫn Hệ Thống Dashboard Theo Role

## Tổng Quan

Hệ thống đã được cập nhật để sử dụng dữ liệu thực từ database và tạo giao diện riêng biệt cho từng role dựa trên chức năng trong database.

## Cấu Trúc Giao Diện

### 1. Customer Dashboard (`/dashboard`)
**Template:** `customer/customer-dashboard.html`

**Chức năng dựa trên database:**
- **My Bookings**: Quản lý đặt xe của khách hàng
- **Favorites**: Xe yêu thích (từ bảng `UserFavoriteVehicles`)
- **Reviews**: Đánh giá xe (từ bảng `UserFeedback`)
- **Profile**: Thông tin cá nhân

**Dữ liệu hiển thị:**
- Active Bookings (từ bảng `Booking` với status = 'Confirmed' hoặc 'Pending')
- Total Bookings (tất cả booking của user)
- Favorite Vehicles (từ bảng `UserFavoriteVehicles`)
- Reviews Given (từ bảng `UserFeedback`)

### 2. Staff Dashboard (`/staff`)
**Template:** `staff/staff-dashboard.html`

**Chức năng dựa trên database:**
- **Vehicle Management**: Quản lý xe (từ bảng `Vehicle`)
- **Booking Management**: Quản lý đặt xe (từ bảng `Booking`)
- **Maintenance**: Bảo trì xe (từ bảng `VehicleConditionLogs`, `BatteryLogs`)
- **Reports**: Báo cáo thống kê

**Dữ liệu hiển thị:**
- Total Vehicles (từ bảng `Vehicle`)
- Active Bookings (từ bảng `Booking` với status = 'Confirmed')
- Pending Bookings (từ bảng `Booking` với status = 'Pending')
- Today's Revenue (tính từ bảng `Booking` và `Payment`)

### 3. Admin Dashboard (`/admin`)
**Template:** `admin/admin-dashboard.html`

**Chức năng dựa trên database:**
- **System Overview**: Tổng quan hệ thống
- **User Management**: Quản lý người dùng (từ bảng `Users`)
- **Vehicle Management**: Quản lý xe (từ bảng `Vehicle`)
- **Booking Management**: Quản lý đặt xe (từ bảng `Booking`)
- **Reports**: Báo cáo toàn hệ thống
- **Settings**: Cài đặt hệ thống

**Dữ liệu hiển thị:**
- Total Users (từ bảng `Users`)
- Total Vehicles (từ bảng `Vehicle`)
- Total Bookings (từ bảng `Booking`)
- Total Revenue (từ bảng `Payment`)

## Cách Sử Dụng

### 1. Tạo User Test
```bash
# Truy cập URL để tạo user test
http://localhost:8080/test/create-users
```

### 2. Login Test Nhanh
```bash
# Customer
http://localhost:8080/test/login?role=customer

# Staff  
http://localhost:8080/test/login?role=staff

# Admin
http://localhost:8080/test/login?role=admin
```

### 3. Login Thông Thường
- Truy cập: `http://localhost:8080/login`
- Email: customer@test.com, staff@test.com, admin@test.com
- Password: password123

## Luồng Hoạt Động

### 1. Đăng Nhập
1. User đăng nhập qua `/login`
2. `AuthController` xác thực và load user với role từ database
3. Lưu user vào session
4. Redirect đến `/dashboard`

### 2. Dashboard Routing
1. `DashboardRouterController` kiểm tra role từ database
2. Redirect đến dashboard phù hợp:
   - **Customer** → `/dashboard` (Customer Dashboard)
   - **Staff** → `/staff` (Staff Dashboard)
   - **Admin** → `/admin` (Admin Dashboard)

### 3. Hiển Thị Dữ Liệu
1. Mỗi controller load dữ liệu từ database dựa trên role
2. Truyền dữ liệu vào template tương ứng
3. Template hiển thị giao diện phù hợp với chức năng

## Database Schema Sử Dụng

### Bảng Chính
- **Users**: Thông tin người dùng
- **Roles**: Phân quyền (Customer, Staff, Admin)
- **Vehicle**: Thông tin xe
- **Booking**: Đặt xe
- **Payment**: Thanh toán
- **UserFavoriteVehicles**: Xe yêu thích
- **UserFeedback**: Đánh giá
- **VehicleConditionLogs**: Log tình trạng xe
- **BatteryLogs**: Log pin xe

### Quan Hệ
- `Users.RoleId` → `Roles.RoleId`
- `Booking.UserId` → `Users.UserId`
- `Booking.VehicleId` → `Vehicle.VehicleId`
- `Payment.BookingId` → `Booking.BookingId`

## Tính Năng Mới

### 1. Role-Based UI
- Mỗi role có giao diện riêng biệt
- Chức năng phù hợp với quyền hạn
- Dữ liệu được filter theo role

### 2. Real Database Integration
- Sử dụng dữ liệu thực từ database
- Không còn hardcode role IDs
- Dynamic role loading

### 3. Enhanced Data Display
- Statistics cards với dữ liệu thực
- Tables với pagination
- Charts và graphs (placeholder)
- Real-time data updates

### 4. Improved Navigation
- Role-based menu items
- Smart routing
- Breadcrumb navigation

## API Endpoints

### Authentication
- `GET /login` - Trang đăng nhập
- `POST /login` - Xử lý đăng nhập
- `GET /logout` - Đăng xuất

### Dashboards
- `GET /dashboard` - Customer dashboard
- `GET /staff` - Staff dashboard
- `GET /admin` - Admin dashboard

### Test
- `GET /test/create-users` - Tạo user test
- `GET /test/login?role={role}` - Login test nhanh

## Cấu Hình

### Database
- Đảm bảo chạy `insert_sample_data.sql` trước
- Roles được tạo tự động: Customer, Staff, Admin
- Sample data cho vehicles, bookings, users

### Application
- Spring Boot với JPA
- Thymeleaf templates
- Bootstrap CSS framework
- Font Awesome icons

## Troubleshooting

### 1. Role không load được
- Kiểm tra database có dữ liệu roles
- Chạy lại `insert_sample_data.sql`
- Kiểm tra JPA mapping

### 2. Giao diện không hiển thị
- Kiểm tra template path
- Kiểm tra controller return value
- Kiểm tra Thymeleaf configuration

### 3. Dữ liệu không đúng
- Kiểm tra service methods
- Kiểm tra database queries
- Kiểm tra data filtering logic

## Mở Rộng

### Thêm Role Mới
1. Thêm role vào database
2. Tạo template mới
3. Cập nhật controller
4. Thêm routing logic

### Thêm Chức Năng
1. Thêm method vào service
2. Cập nhật controller
3. Cập nhật template
4. Thêm database queries nếu cần
