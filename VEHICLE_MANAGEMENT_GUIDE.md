# 🚗 Hướng Dẫn Quản Lý Xe - EcoDana Admin

## Tổng Quan

Hệ thống quản lý xe điện EcoDana cung cấp giao diện quản trị toàn diện để quản lý danh sách xe điện (ô tô và xe máy) với đầy đủ chức năng CRUD (Create, Read, Update, Delete).

## 📋 Mục Lục

1. [Tính Năng Chính](#tính-năng-chính)
2. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
3. [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
4. [API Endpoints](#api-endpoints)
5. [Cấu Trúc Database](#cấu-trúc-database)
6. [Bảo Mật](#bảo-mật)

## ✨ Tính Năng Chính

### 1. Quản Lý Danh Sách Xe
- **Hiển thị danh sách**: Giao diện card hiện đại với thông tin đầy đủ
- **Tìm kiếm**: Tìm theo tên xe, biển số (có debounce 300ms)
- **Lọc**: Theo loại xe (ô tô/xe máy) và trạng thái
- **Thống kê**: Hiển thị số lượng xe theo trạng thái

### 2. Thêm Xe Mới
- Form nhập liệu đầy đủ với validation
- Hỗ trợ upload nhiều ảnh
- Thiết lập giá thuê theo giờ/ngày/tháng
- Phân loại theo danh mục và loại hộp số

### 3. Chỉnh Sửa Xe
- Cập nhật thông tin xe hiện có
- Kiểm tra trùng biển số
- Lưu lịch sử thay đổi

### 4. Xem Chi Tiết
- Hiển thị đầy đủ thông tin xe
- Gallery ảnh
- Thông tin kỹ thuật
- Lịch sử thuê (tương lai)

### 5. Xóa Xe
- Modal xác nhận trước khi xóa
- Kiểm tra ràng buộc với booking
- Soft delete (có thể cấu hình)

## 🏗️ Kiến Trúc Hệ Thống

### Backend Structure

```
src/main/java/com/ecodana/evodanavn1/
├── controller/
│   └── admin/
│       └── VehicleAdminController.java    # REST API endpoints
├── service/
│   └── VehicleService.java                # Business logic
├── repository/
│   ├── VehicleRepository.java             # Data access
│   ├── VehicleCategoryRepository.java
│   └── TransmissionTypeRepository.java
├── model/
│   ├── Vehicle.java                       # Entity
│   ├── VehicleCategory.java
│   └── TransmissionType.java
└── dto/
    ├── VehicleRequest.java                # Request DTO
    └── VehicleResponse.java               # Response DTO
```

### Frontend Structure

```
src/main/resources/
├── templates/admin/
│   ├── admin-vehicles-management.html     # Main list page
│   ├── vehicle-add.html                   # Add form
│   ├── vehicle-edit.html                  # Edit form
│   └── vehicle-detail.html                # Detail view
├── static/
│   ├── css/
│   │   └── admin-vehicles.css             # Styles
│   └── js/
│       └── admin-vehicles.js              # Client logic
```

## 📖 Hướng Dẫn Sử Dụng

### Truy Cập Trang Quản Lý

1. Đăng nhập với tài khoản Admin
2. Truy cập: `http://localhost:8080/admin/vehicles`

### Thêm Xe Mới

1. Click nút **"Thêm Xe Mới"**
2. Điền đầy đủ thông tin:
   - **Thông tin cơ bản**: Tên xe, biển số, loại xe, năm sản xuất
   - **Thông số kỹ thuật**: Số chỗ ngồi, số km đã đi, dung lượng pin
   - **Phân loại**: Danh mục, loại hộp số
   - **Giá thuê**: Theo giờ, ngày, tháng
   - **Hình ảnh**: URL ảnh chính và ảnh phụ
   - **Tính năng**: Các tính năng đặc biệt
   - **Mô tả**: Mô tả chi tiết
3. Click **"Lưu Xe"**

### Chỉnh Sửa Xe

1. Tìm xe cần sửa trong danh sách
2. Click nút **"Sửa"** trên card xe
3. Cập nhật thông tin cần thiết
4. Click **"Cập Nhật"**

### Xem Chi Tiết

1. Click nút **"Chi Tiết"** trên card xe
2. Xem đầy đủ thông tin và ảnh
3. Có thể chuyển sang chỉnh sửa hoặc xóa từ trang này

### Xóa Xe

1. Click nút **"Xóa"** trên card xe
2. Xác nhận trong modal popup
3. Xe sẽ bị xóa khỏi hệ thống

### Tìm Kiếm và Lọc

- **Tìm kiếm**: Gõ tên xe hoặc biển số vào ô tìm kiếm
- **Lọc theo loại**: Chọn "Xe Ô Tô Điện" hoặc "Xe Máy Điện"
- **Lọc theo trạng thái**: Chọn Available, Rented, Maintenance, Unavailable
- **Xóa bộ lọc**: Click "Xóa Bộ Lọc"

### Phím Tắt

- **Ctrl/Cmd + K**: Focus vào ô tìm kiếm
- **ESC**: Đóng modal

## 🔌 API Endpoints

### GET Endpoints

```http
GET /admin/vehicles                    # Trang quản lý xe
GET /admin/vehicles/add                # Trang thêm xe
GET /admin/vehicles/edit/{id}          # Trang sửa xe
GET /admin/vehicles/detail/{id}        # Trang chi tiết xe
GET /admin/vehicles/api/list           # API lấy danh sách xe
GET /admin/vehicles/api/{id}           # API lấy thông tin xe
GET /admin/vehicles/api/search?keyword # API tìm kiếm xe
GET /admin/vehicles/api/categories     # API lấy danh mục
GET /admin/vehicles/api/transmission-types # API lấy loại hộp số
```

### POST Endpoints

```http
POST /admin/vehicles/api/create        # Tạo xe mới
```

**Request Body:**
```json
{
  "vehicleModel": "VinFast VF8",
  "licensePlate": "43A-12345",
  "vehicleType": "ElectricCar",
  "yearManufactured": 2023,
  "seats": 5,
  "odometer": 5000,
  "batteryCapacity": 87.7,
  "status": "Available",
  "categoryId": 2,
  "transmissionTypeId": 4,
  "requiresLicense": true,
  "hourlyPrice": 80000,
  "dailyPrice": 800000,
  "monthlyPrice": 20000000,
  "mainImageUrl": "https://example.com/image.jpg",
  "imageUrls": ["url1", "url2"],
  "features": ["GPS", "Camera 360"],
  "description": "Mô tả xe"
}
```

### PUT Endpoints

```http
PUT /admin/vehicles/api/update/{id}    # Cập nhật xe
```

### DELETE Endpoints

```http
DELETE /admin/vehicles/api/delete/{id} # Xóa xe
```

### PATCH Endpoints

```http
PATCH /admin/vehicles/api/status/{id}?status=Available # Cập nhật trạng thái
```

## 🗄️ Cấu Trúc Database

### Bảng Vehicle

```sql
CREATE TABLE Vehicle (
  VehicleId CHAR(36) PRIMARY KEY,
  VehicleModel VARCHAR(50) NOT NULL,
  YearManufactured INT,
  LicensePlate VARCHAR(20) NOT NULL UNIQUE,
  Seats INT NOT NULL,
  Odometer INT NOT NULL,
  RentalPrices JSON,
  Status ENUM('Available', 'Rented', 'Maintenance', 'Unavailable'),
  Description VARCHAR(500),
  VehicleType ENUM('ElectricCar', 'ElectricMotorcycle'),
  RequiresLicense BOOLEAN DEFAULT TRUE,
  BatteryCapacity DECIMAL(10,2),
  CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
  CategoryId INT,
  TransmissionTypeId INT,
  MainImageUrl VARCHAR(255),
  ImageUrls JSON,
  Features JSON,
  FOREIGN KEY (CategoryId) REFERENCES VehicleCategories(CategoryId),
  FOREIGN KEY (TransmissionTypeId) REFERENCES TransmissionTypes(TransmissionTypeId)
);
```

### Bảng VehicleCategories

```sql
CREATE TABLE VehicleCategories (
  CategoryId INT PRIMARY KEY AUTO_INCREMENT,
  CategoryName VARCHAR(100) NOT NULL UNIQUE
);
```

### Bảng TransmissionTypes

```sql
CREATE TABLE TransmissionTypes (
  TransmissionTypeId INT PRIMARY KEY AUTO_INCREMENT,
  TransmissionTypeName VARCHAR(100) NOT NULL UNIQUE
);
```

## 🔒 Bảo Mật

### Authentication & Authorization

- Tất cả endpoints yêu cầu đăng nhập
- Chỉ user có role **Admin** mới truy cập được
- Session-based authentication

### Input Validation

- Server-side validation cho tất cả input
- Kiểm tra biển số trùng lặp
- Sanitize HTML để tránh XSS

### CSRF Protection

- Spring Security CSRF token
- Tự động thêm vào mọi request

### SQL Injection Prevention

- Sử dụng JPA/Hibernate với parameterized queries
- Không có raw SQL queries

## 🎨 Tùy Chỉnh

### Thay Đổi Màu Sắc

Chỉnh sửa file `admin-vehicles.css`:

```css
:root {
    --primary-green: #16a34a;      /* Màu chính */
    --primary-green-dark: #15803d;  /* Màu tối */
    --primary-green-light: #22c55e; /* Màu sáng */
}
```

### Thêm Trường Mới

1. Thêm column vào database
2. Cập nhật `Vehicle.java` entity
3. Cập nhật `VehicleRequest.java` và `VehicleResponse.java`
4. Cập nhật form HTML
5. Cập nhật `VehicleService.java`

## 🐛 Xử Lý Lỗi

### Lỗi Thường Gặp

1. **Biển số trùng**: Kiểm tra biển số đã tồn tại
2. **Validation lỗi**: Kiểm tra required fields
3. **Unauthorized**: Đảm bảo đã đăng nhập với role Admin
4. **404 Not Found**: Xe không tồn tại trong database

### Debug Mode

Bật logging trong `application.properties`:

```properties
logging.level.com.ecodana.evodanavn1=DEBUG
```

## 📊 Performance

### Optimization

- **Debounced search**: 300ms delay
- **Lazy loading**: Images load on demand
- **Pagination**: Sẽ thêm cho danh sách lớn
- **Caching**: Redis cache cho danh sách xe (tương lai)

### Best Practices

- Giới hạn số lượng ảnh upload
- Compress ảnh trước khi lưu
- Index database cho search fields
- Use CDN cho static assets

## 🚀 Deployment

### Production Checklist

- [ ] Cấu hình database connection pool
- [ ] Enable HTTPS
- [ ] Set up backup schedule
- [ ] Configure logging
- [ ] Set up monitoring
- [ ] Enable rate limiting
- [ ] Optimize images
- [ ] Minify CSS/JS

## 📞 Hỗ Trợ

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs
2. Xem documentation
3. Liên hệ team phát triển

---

**Version**: 1.0.0  
**Last Updated**: 2025-10-10  
**Author**: EcoDana Development Team
