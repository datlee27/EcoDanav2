# CHÍNH SÁCH HỦY XE

## 📋 Quy tắc hủy xe

### **Rule 1: Đặt trong vòng 24 giờ trước ngày nhận**
- ❌ **KHÔNG được hủy**
- Lý do: Quá gần ngày nhận xe

**Ví dụ:**
```
Ngày nhận xe: 15/11/2025 21:00
Ngày đặt: 15/11/2025 10:00 (11 giờ trước)
→ ❌ KHÔNG thể hủy
```

### **Rule 2: Đặt trước 1-2 ngày (24-48 giờ)**
- ✅ Có thể hủy
- ⏰ Phải hủy trước ngày nhận **ít nhất 8 tiếng**

**Ví dụ:**
```
Ngày nhận xe: 15/11/2025 21:00
Ngày đặt: 14/11/2025 10:00 (35 giờ trước)

Scenario A: Hủy lúc 15/11/2025 10:00 (11 giờ trước nhận)
→ ✅ Được hủy

Scenario B: Hủy lúc 15/11/2025 18:00 (3 giờ trước nhận)
→ ❌ KHÔNG được hủy (phải hủy trước 8 tiếng)
```

### **Rule 3: Đặt trước > 2 ngày (> 48 giờ)**
- ✅ Có thể hủy
- ⏰ Phải hủy trước ngày nhận **ít nhất 24 tiếng**

**Ví dụ:**
```
Ngày nhận xe: 15/11/2025 21:00
Ngày đặt: 12/11/2025 10:00 (83 giờ trước)

Scenario A: Hủy lúc 14/11/2025 10:00 (35 giờ trước nhận)
→ ✅ Được hủy

Scenario B: Hủy lúc 15/11/2025 10:00 (11 giờ trước nhận)
→ ❌ KHÔNG được hủy (phải hủy trước 24 tiếng)
```

## 🔧 Implementation

### **Backend Logic:**

```java
// Calculate hours
long hoursUntilPickup = Duration.between(now, pickupDateTime).toHours();
long hoursFromCreationToPickup = Duration.between(createdDate, pickupDateTime).toHours();

// Rule 1: < 24 hours from creation to pickup
if (hoursFromCreationToPickup < 24) {
    return "Không thể hủy! Đơn đặt xe trong vòng 24 giờ trước ngày nhận không được hủy.";
}

// Rule 2: 24-48 hours from creation to pickup
if (hoursFromCreationToPickup >= 24 && hoursFromCreationToPickup < 48) {
    if (hoursUntilPickup < 8) {
        return "Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 8 tiếng.";
    }
}

// Rule 3: > 48 hours from creation to pickup
if (hoursFromCreationToPickup >= 48) {
    if (hoursUntilPickup < 24) {
        return "Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 24 tiếng.";
    }
}
```

## 📊 Bảng tổng hợp

| Thời gian đặt trước | Thời gian hủy tối thiểu | Ví dụ |
|---------------------|-------------------------|-------|
| < 24 giờ | ❌ Không được hủy | Đặt hôm nay, nhận mai |
| 24-48 giờ | 8 giờ trước nhận | Đặt 1.5 ngày trước |
| > 48 giờ | 24 giờ trước nhận | Đặt 3 ngày trước |

## 💡 Thông báo lỗi

### **Error Messages:**

1. **Đặt trong 24h:**
   ```
   ❌ Không thể hủy! Đơn đặt xe trong vòng 24 giờ trước ngày nhận không được hủy.
   ```

2. **Đặt 1-2 ngày, hủy muộn:**
   ```
   ❌ Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 8 tiếng.
   ```

3. **Đặt > 2 ngày, hủy muộn:**
   ```
   ❌ Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 24 tiếng.
   ```

4. **Thành công:**
   ```
   ✅ Đã hủy booking thành công!
   ```

## 🎯 Test Cases

### **Test 1: Đặt trong 24h**
```
Created: 15/11/2025 10:00
Pickup: 15/11/2025 21:00
Cancel at: 15/11/2025 15:00
→ ❌ FAIL: "Đơn đặt xe trong vòng 24 giờ..."
```

### **Test 2: Đặt 1.5 ngày, hủy 10h trước**
```
Created: 14/11/2025 10:00
Pickup: 15/11/2025 21:00
Cancel at: 15/11/2025 11:00 (10h trước)
→ ✅ PASS: Hủy thành công
```

### **Test 3: Đặt 1.5 ngày, hủy 5h trước**
```
Created: 14/11/2025 10:00
Pickup: 15/11/2025 21:00
Cancel at: 15/11/2025 16:00 (5h trước)
→ ❌ FAIL: "Phải hủy trước... ít nhất 8 tiếng"
```

### **Test 4: Đặt 3 ngày, hủy 30h trước**
```
Created: 12/11/2025 10:00
Pickup: 15/11/2025 21:00
Cancel at: 14/11/2025 15:00 (30h trước)
→ ✅ PASS: Hủy thành công
```

### **Test 5: Đặt 3 ngày, hủy 12h trước**
```
Created: 12/11/2025 10:00
Pickup: 15/11/2025 21:00
Cancel at: 15/11/2025 09:00 (12h trước)
→ ❌ FAIL: "Phải hủy trước... ít nhất 24 tiếng"
```

## 🚀 Deployment

### **Files Changed:**
- `BookingController.java` - Added cancellation policy logic

### **No Database Changes Required**
- Uses existing fields: `CreatedDate`, `PickupDateTime`

### **Testing:**
1. Restart application
2. Create bookings with different timings
3. Try to cancel and verify error messages

## ✅ Checklist

- [x] Rule 1: Block cancellation if booked < 24h before pickup
- [x] Rule 2: Require 8h notice for bookings 24-48h before pickup
- [x] Rule 3: Require 24h notice for bookings > 48h before pickup
- [x] Error messages for each rule
- [x] Success message when cancellation allowed

**Chính sách hủy xe đã được implement!** 🎉
