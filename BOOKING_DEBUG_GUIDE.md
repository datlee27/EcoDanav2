# Hướng dẫn Debug vấn đề Booking không chuyển đến trang thanh toán

## Vấn đề
Khi xác nhận đặt xe, thay vì chuyển đến trang chọn phương thức thanh toán (`/payment/select-method/{bookingId}`), 
hệ thống redirect về trang vehicle detail.

## Nguyên nhân có thể
1. ❌ Form không submit đúng data (thiếu field hoặc data null)
2. ❌ Exception xảy ra trong controller `BookingController.createBooking()`
3. ❌ Database constraint violation (thiếu cột PaymentStatus)
4. ❌ Validation fail (ngày giờ, xe không available, etc.)

## Các bước Debug

### Bước 1: Kiểm tra Console Log (Browser)
1. Mở Developer Tools (F12)
2. Vào tab Console
3. Click "Xác nhận đặt xe"
4. Xem log `=== Form Data Before Submit ===`
5. Kiểm tra xem tất cả các field có giá trị không:
   - vehicleId
   - pickupDate
   - pickupTime
   - returnDate
   - returnTime
   - pickupLocation
   - totalAmount
   - rentalDays
   - additionalInsurance
   - discountId
   - discountAmount

### Bước 2: Kiểm tra Server Log
1. Mở terminal/console nơi chạy Spring Boot
2. Tìm log `=== Creating booking ===`
3. Xem các giá trị được print ra:
   ```
   Vehicle ID: ...
   Pickup Date: ...
   Pickup Time: ...
   Return Date: ...
   Return Time: ...
   Total Amount: ...
   Rental Days: ...
   ```
4. Nếu có lỗi, sẽ thấy:
   ```
   === ERROR creating booking ===
   Error: [error message]
   [stack trace]
   ```

### Bước 3: Kiểm tra Database
Chạy query để xem cột PaymentStatus có tồn tại không:
```sql
SHOW COLUMNS FROM Booking LIKE 'PaymentStatus';
```

Nếu không có, chạy migration:
```bash
cd c:\EcoDanav2
run-vnpay-migration.bat
```

Hoặc chạy trực tiếp SQL:
```sql
ALTER TABLE Booking 
ADD COLUMN PaymentStatus VARCHAR(20) DEFAULT 'Unpaid';
```

### Bước 4: Test với data đơn giản
Thử tạo booking với data tối thiểu để xem có lỗi gì không.

## Các lỗi thường gặp và cách fix

### Lỗi 1: "Thiếu thông tin xe!"
**Nguyên nhân:** vehicleId bị null hoặc empty
**Fix:** Kiểm tra form có field `vehicleId` và có giá trị không

### Lỗi 2: "Thiếu thông tin ngày giờ thuê xe!"
**Nguyên nhân:** Một trong các field date/time bị null
**Fix:** Kiểm tra form có đầy đủ:
- pickupDate
- pickupTime
- returnDate
- returnTime

### Lỗi 3: "Thiếu thông tin tổng tiền!"
**Nguyên nhân:** totalAmount bị null
**Fix:** Kiểm tra calculation và form có field totalAmount

### Lỗi 4: "Ngày nhận xe không thể là quá khứ!"
**Nguyên nhân:** pickupDateTime < now()
**Fix:** Chọn ngày giờ trong tương lai

### Lỗi 5: "Ngày trả xe phải sau ngày nhận xe!"
**Nguyên nhân:** returnDateTime < pickupDateTime
**Fix:** Chọn ngày trả sau ngày nhận

### Lỗi 6: "Không tìm thấy xe!"
**Nguyên nhân:** Vehicle không tồn tại trong database
**Fix:** Kiểm tra vehicleId có đúng không

### Lỗi 7: "Xe không khả dụng để đặt!"
**Nguyên nhân:** Vehicle status != "Available"
**Fix:** Chọn xe khác hoặc update vehicle status

### Lỗi 8: SQL Exception - Column 'PaymentStatus' doesn't exist
**Nguyên nhân:** Chưa chạy migration
**Fix:** Chạy `run-vnpay-migration.bat`

## Code đã được update

### 1. BookingController.java
- ✅ Thêm validation chi tiết cho tất cả required fields
- ✅ Thêm debug logging để track data
- ✅ Thêm error messages rõ ràng hơn

### 2. booking-checkout.html
- ✅ Thêm console.log để debug form data
- ✅ Form có đầy đủ hidden fields

## Kiểm tra nhanh

Chạy các lệnh sau để kiểm tra:

```bash
# 1. Kiểm tra app có đang chạy không
curl http://localhost:8080

# 2. Kiểm tra database connection
# Mở MySQL Workbench hoặc command line
mysql -u root -p ecodana

# 3. Kiểm tra table structure
DESCRIBE Booking;

# 4. Kiểm tra có booking nào được tạo không
SELECT * FROM Booking ORDER BY CreatedDate DESC LIMIT 5;
```

## Liên hệ
Nếu vẫn gặp lỗi, hãy cung cấp:
1. Screenshot của Console log (Browser)
2. Server log (từ terminal)
3. Error message cụ thể
