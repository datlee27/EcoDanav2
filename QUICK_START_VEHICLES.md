# 🚀 Quick Start - Hệ Thống Quản Lý Xe

## Bắt Đầu Nhanh trong 5 Phút

### Bước 1: Chuẩn Bị Database

```sql
-- 1. Tạo database (nếu chưa có)
CREATE DATABASE ecodanangv2;
USE ecodanangv2;

-- 2. Chạy schema chính
SOURCE src/main/resources/db/newdata.sql;

-- 3. Thêm dữ liệu mẫu
SOURCE src/main/resources/db/vehicle-sample-data.sql;
```

### Bước 2: Khởi Động Application

```bash
# Từ thư mục root của project
mvn clean install
mvn spring-boot:run
```

### Bước 3: Truy Cập Admin Panel

1. Mở browser: `http://localhost:8080/admin/vehicles`
2. Đăng nhập với tài khoản Admin
3. Bắt đầu quản lý xe!

## 📋 Checklist Kiểm Tra

- [ ] Database đã được tạo và có dữ liệu
- [ ] Application chạy không lỗi
- [ ] Có thể truy cập `/admin/vehicles`
- [ ] Danh sách xe hiển thị đúng
- [ ] Search và filter hoạt động
- [ ] Có thể thêm xe mới
- [ ] Có thể sửa xe
- [ ] Có thể xem chi tiết
- [ ] Có thể xóa xe

## 🎯 Thử Nghiệm Nhanh

### Test 1: Xem Danh Sách
✅ Truy cập `/admin/vehicles` → Thấy 10 xe mẫu

### Test 2: Tìm Kiếm
✅ Gõ "VinFast" vào search → Thấy các xe VinFast

### Test 3: Lọc
✅ Chọn "Xe Ô Tô Điện" → Chỉ thấy ô tô
✅ Chọn "Available" → Chỉ thấy xe sẵn sàng

### Test 4: Thêm Xe
✅ Click "Thêm Xe Mới"
✅ Điền form:
- Tên: "Test Vehicle"
- Biển số: "99Z-99999"
- Loại: "ElectricCar"
- Năm: 2024
- Số chỗ: 5
- Km: 0
- Giá/ngày: 500000
✅ Click "Lưu Xe" → Thành công

### Test 5: Sửa Xe
✅ Click "Sửa" trên xe vừa tạo
✅ Đổi tên thành "Test Vehicle Updated"
✅ Click "Cập Nhật" → Thành công

### Test 6: Xem Chi Tiết
✅ Click "Chi Tiết" → Thấy đầy đủ thông tin

### Test 7: Xóa Xe
✅ Click "Xóa" → Modal hiện ra
✅ Click "Xóa" trong modal → Xe bị xóa

## 🔧 Troubleshooting

### Lỗi: Cannot connect to database
```bash
# Kiểm tra MySQL đang chạy
mysql -u root -p

# Kiểm tra connection trong application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecodanangv2
spring.datasource.username=root
spring.datasource.password=your_password
```

### Lỗi: 404 Not Found
```bash
# Kiểm tra controller đã được scan
@ComponentScan(basePackages = "com.ecodana.evodanavn1")

# Kiểm tra URL mapping
/admin/vehicles (không có dấu / ở cuối)
```

### Lỗi: Unauthorized
```bash
# Đảm bảo đã đăng nhập với role Admin
# Kiểm tra session
# Clear browser cache và cookies
```

### Lỗi: JavaScript không hoạt động
```bash
# Kiểm tra console browser (F12)
# Đảm bảo file JS được load
# Check network tab xem có lỗi 404 không
```

## 📚 Tài Liệu Chi Tiết

- **Full Guide**: `VEHICLE_MANAGEMENT_GUIDE.md`
- **System Summary**: `VEHICLE_SYSTEM_SUMMARY.md`
- **Database Schema**: `src/main/resources/db/newdata.sql`
- **Sample Data**: `src/main/resources/db/vehicle-sample-data.sql`

## 💡 Tips

1. **Keyboard Shortcuts**:
   - `Ctrl/Cmd + K`: Focus search
   - `ESC`: Close modal

2. **Best Practices**:
   - Luôn điền đầy đủ thông tin bắt buộc
   - Sử dụng biển số đúng format
   - Upload ảnh chất lượng cao
   - Viết mô tả chi tiết

3. **Performance**:
   - Search tự động sau 300ms
   - Filters apply ngay lập tức
   - Pagination sẽ có trong phiên bản sau

## 🎉 Hoàn Thành!

Bạn đã sẵn sàng sử dụng hệ thống quản lý xe EcoDana!

**Next Steps**:
1. Thêm xe thật vào hệ thống
2. Tùy chỉnh theo nhu cầu
3. Tích hợp với booking system
4. Deploy lên production

---

**Need Help?** Check `VEHICLE_MANAGEMENT_GUIDE.md` hoặc liên hệ dev team.
