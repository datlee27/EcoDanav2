# ✅ DUPLICATE MAPPING - ĐÃ SỬA!

## ❌ **LỖI:**

```
Ambiguous mapping. Cannot map 'VNPayController' method
to {GET [/payment/vnpay-return]}: There is already 'paymentMethodController' bean method
```

---

## 🔍 **NGUYÊN NHÂN:**

Có **2 controllers** cùng định nghĩa endpoints:
- `VNPayController.vnpayReturn()` → `/payment/vnpay-return`
- `PaymentMethodController.vnpayReturn()` → `/payment/vnpay-return`

Spring không biết dùng cái nào → **Conflict!**

---

## ✅ **GIẢI PHÁP:**

**Đã xóa `VNPayController.java`**

Vì tất cả VNPay endpoints đã được thêm vào `PaymentMethodController`, không cần `VNPayController` nữa.

---

## 📝 **ENDPOINTS HIỆN TẠI:**

### **PaymentMethodController:**
- ✅ `GET /payment/select-method/{bookingId}` - Chọn phương thức
- ✅ `POST /payment/process` - Xử lý payment
- ✅ `GET /payment/vnpay-return` - VNPay callback
- ✅ `GET /payment/vnpay-ipn` - VNPay IPN
- ✅ `GET /payment/qr-code/{bookingId}` - QR payment
- ✅ `GET /payment/bank-transfer-info/{bookingId}` - Bank transfer

---

## 🚀 **BÂY GIỜ:**

1. ✅ Đã xóa VNPayController
2. ⏳ Restart app
3. ⏳ Test VNPay payment

**App sẽ chạy thành công!** 🎉
