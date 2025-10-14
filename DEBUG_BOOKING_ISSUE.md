# 🔍 DEBUG - Booking Creation Issue

## Vấn đề
Sau khi click "Xác nhận đặt xe", trang redirect về vehicle detail thay vì payment selection.

## Nguyên nhân
Có exception xảy ra trong `BookingController.createBooking()` → Rơi vào catch block → Redirect về `/vehicles/{vehicleId}`

## Bước Debug

### 1. Kiểm tra Console Log

Khi click "Xác nhận đặt xe", xem console của application (terminal đang chạy Spring Boot):

```
Tìm dòng:
=== ERROR creating booking ===
Error: [Thông báo lỗi ở đây]
```

### 2. Các lỗi thường gặp

#### Lỗi 1: Column 'PaymentStatus' doesn't exist
```
Error: Column 'PaymentStatus' doesn't exist in table 'Booking'
```

**Giải pháp**: Chạy migration script
```sql
-- Mở MySQL và chạy:
USE ecodanav2;  -- Hoặc tên database của bạn

ALTER TABLE Booking 
ADD COLUMN PaymentStatus VARCHAR(20) DEFAULT 'Unpaid';
```

#### Lỗi 2: NULL value in required field
```
Error: Column 'xxx' cannot be null
```

**Giải pháp**: Kiểm tra form có đầy đủ hidden inputs không

#### Lỗi 3: User session null
```
Error: Cannot invoke "com.ecodana.evodanavn1.model.User.getId()" because "user" is null
```

**Giải pháp**: Đảm bảo đã login

### 3. Kiểm tra Database

```sql
-- 1. Kiểm tra cột PaymentStatus có tồn tại không
DESCRIBE Booking;

-- Kết quả mong đợi: Phải có cột PaymentStatus

-- 2. Kiểm tra bảng payment
DESCRIBE payment;

-- 3. Test tạo booking thủ công
INSERT INTO Booking (
    BookingId, UserId, VehicleId, 
    PickupDateTime, ReturnDateTime, 
    TotalAmount, Status, BookingCode, 
    PaymentStatus, RentalType, 
    CreatedDate, TermsAgreed
) VALUES (
    'test-123', 
    'your-user-id',  -- Thay bằng user ID thật
    'your-vehicle-id',  -- Thay bằng vehicle ID thật
    '2025-01-20 10:00:00', 
    '2025-01-25 10:00:00',
    1000000, 
    'Pending', 
    'BK_TEST_123',
    'Unpaid',
    'daily',
    NOW(),
    1
);

-- Nếu INSERT thành công → Database OK
-- Nếu lỗi → Xem lỗi gì
```

### 4. Kiểm tra Form Data

Mở Developer Tools (F12) → Network tab → Click "Xác nhận đặt xe" → Xem request:

**Kiểm tra POST data có đầy đủ không:**
- vehicleId
- pickupDate
- pickupTime
- returnDate
- returnTime
- pickupLocation
- totalAmount
- rentalDays
- additionalInsurance
- discountId (có thể empty)
- discountAmount (có thể 0)

### 5. Thêm Debug Log

Nếu vẫn không rõ lỗi, thêm log vào `BookingController.java`:

```java
@PostMapping("/create")
public String createBooking(@ModelAttribute BookingRequest bookingRequest, 
                           HttpSession session, 
                           RedirectAttributes redirectAttributes) {
    
    System.out.println("=== DEBUG: Starting booking creation ===");
    
    User user = (User) session.getAttribute("currentUser");
    System.out.println("DEBUG: User = " + (user != null ? user.getId() : "NULL"));
    
    if (user == null) {
        System.out.println("DEBUG: User is null, redirecting to login");
        redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt xe!");
        return "redirect:/login";
    }

    try {
        System.out.println("DEBUG: VehicleId = " + bookingRequest.getVehicleId());
        System.out.println("DEBUG: PickupDate = " + bookingRequest.getPickupDate());
        System.out.println("DEBUG: ReturnDate = " + bookingRequest.getReturnDate());
        System.out.println("DEBUG: TotalAmount = " + bookingRequest.getTotalAmount());
        
        // Parse dates and times
        LocalDate pickup = LocalDate.parse(bookingRequest.getPickupDate());
        System.out.println("DEBUG: Parsed pickup date successfully");
        
        LocalDate returnD = LocalDate.parse(bookingRequest.getReturnDate());
        System.out.println("DEBUG: Parsed return date successfully");
        
        // ... rest of code
        
    } catch (Exception e) {
        System.out.println("=== ERROR creating booking ===");
        System.out.println("Error: " + e.getMessage());
        System.out.println("Error class: " + e.getClass().getName());
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        return "redirect:/vehicles/" + bookingRequest.getVehicleId();
    }
}
```

### 6. Quick Fix - Bypass Payment Selection (Temporary)

Nếu cần test nhanh, tạm thời comment redirect:

```java
// Redirect to payment method selection page
// return "redirect:/payment/select-method/" + booking.getBookingId();

// Temporary: redirect to confirmation
return "redirect:/booking/confirmation/" + booking.getBookingId();
```

## Checklist Kiểm Tra

- [ ] Application đang chạy
- [ ] Đã login với user account
- [ ] Database có cột `PaymentStatus` trong bảng `Booking`
- [ ] Form có đầy đủ hidden inputs
- [ ] Xem console log khi click "Xác nhận đặt xe"
- [ ] Check Network tab trong browser
- [ ] Thử INSERT thủ công vào database

## Kết quả mong đợi

Sau khi sửa lỗi:

```
Console log:
=== Booking created successfully ===
Booking ID: xxx-xxx-xxx
Booking Code: BK1234567890
Redirecting to payment method selection

Browser:
URL thay đổi thành: http://localhost:8080/payment/select-method/xxx-xxx-xxx
```

## Liên hệ

Nếu vẫn gặp lỗi, gửi cho tôi:
1. Console log đầy đủ
2. Error message cụ thể
3. Screenshot Network tab (POST request data)
