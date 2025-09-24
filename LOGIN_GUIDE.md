# 🔐 Hướng dẫn Đăng nhập và Phân quyền - EvoDana

## 📋 Tài khoản Test

### 1. **Admin Account**
- **Email:** `admin123@test.com`
- **Password:** `admin123`
- **Quyền:** Toàn quyền hệ thống
- **Chuyển hướng:** `/admin`

### 2. **Owner Account** 
- **Email:** `owner123@test.com`
- **Password:** `owner123`
- **Quyền:** Quản lý xe và đặt chỗ
- **Chuyển hướng:** `/owner/dashboard`

### 3. **Customer Account**
- **Email:** `customer@test.com`
- **Password:** `admin123`
- **Quyền:** Duyệt và đặt xe
- **Chuyển hướng:** `/dashboard`

## 🚀 Cách sử dụng

### Bước 1: Tạo tài khoản test
1. Truy cập: `http://localhost:8080/test-setup`
2. Click "Create All Test Accounts"
3. Đợi thông báo thành công

### Bước 2: Đăng nhập
1. Truy cập: `http://localhost:8080/login`
2. Nhập email và password từ danh sách trên
3. Hệ thống sẽ tự động chuyển hướng theo role

### Bước 3: Test nhanh
1. Truy cập: `http://localhost:8080/test-setup`
2. Click các button "Login as Admin/Owner/Customer"
3. Hệ thống sẽ tự động đăng nhập và chuyển hướng

## 🔧 Các tính năng đã sửa

### ✅ Authentication Flow
- [x] Đăng nhập bằng username hoặc email
- [x] Xác thực password với BCrypt
- [x] Load thông tin role từ database
- [x] Chuyển hướng đúng theo role

### ✅ Role-based Authorization
- [x] Admin: `/admin` - Toàn quyền
- [x] Staff/Owner: `/owner/dashboard` - Quản lý xe
- [x] Customer: `/dashboard` - Duyệt xe

### ✅ Security Features
- [x] Password hashing với BCrypt
- [x] Session management
- [x] Role-based access control
- [x] OAuth2 Google login support

### ✅ Test Tools
- [x] Tạo tài khoản test tự động
- [x] Test login nhanh
- [x] Debug tools
- [x] List users trong database

## 🐛 Debug Tools

### List Users
```
GET /test/list-users
```
Hiển thị tất cả users trong database (check console)

### Test Password
```
GET /test/test-password?email=admin123@test.com&password=admin123
```
Test password matching

### Generate Hash
```
GET /test/generate-hash?password=admin123
```
Tạo BCrypt hash cho password

### Debug Login
```
GET /test/debug-login?email=admin123@test.com&password=admin123
```
Debug từng bước login process

## 📁 File Structure

```
src/main/java/com/ecodana/evodanavn1/
├── controller/
│   ├── AuthController.java          # Xử lý đăng nhập
│   ├── TestController.java          # Tools test
│   ├── DashboardController.java     # Dashboard theo role
│   └── OwnerController.java         # Owner dashboard
├── security/
│   └── OAuth2LoginSuccessHandler.java # OAuth2 login
├── service/
│   ├── UserService.java             # User management
│   └── RoleService.java             # Role management
└── SecurityConfig.java              # Security configuration

src/main/resources/templates/
├── user/login.html                  # Trang đăng nhập
├── test-setup.html                  # Trang test setup
├── admin/admin.html                 # Admin dashboard
├── owner/dashboard.html             # Owner dashboard
└── customer/customer-dashboard.html # Customer dashboard
```

## 🔍 Troubleshooting

### Lỗi "Roles not found"
- Chạy file `src/main/resources/db/insert_sample_data.sql`
- Restart application

### Lỗi "User not found"
- Tạo tài khoản test trước: `/test-setup`
- Check database connection

### Lỗi "Password does not match"
- Sử dụng đúng password từ danh sách
- Check BCrypt encoding

### Lỗi "Access denied"
- User không có role phù hợp
- Check role assignment trong database

## 🎯 Test Cases

1. **Admin Login Test**
   - Email: `admin123@test.com`
   - Password: `admin123`
   - Expected: Redirect to `/admin`

2. **Owner Login Test**
   - Email: `owner123@test.com`
   - Password: `owner123`
   - Expected: Redirect to `/owner/dashboard`

3. **Customer Login Test**
   - Email: `customer@test.com`
   - Password: `admin123`
   - Expected: Redirect to `/dashboard`

4. **Invalid Credentials Test**
   - Email: `invalid@test.com`
   - Password: `wrong`
   - Expected: Error message

5. **OAuth2 Login Test**
   - Click "Sign in with Google"
   - Expected: Auto-create user with Customer role

## ✅ Status: READY TO USE

Hệ thống đăng nhập và phân quyền đã hoàn thiện và sẵn sàng sử dụng!
