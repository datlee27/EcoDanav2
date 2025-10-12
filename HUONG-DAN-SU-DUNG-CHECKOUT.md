# HƯỚNG DẪN SỬ DỤNG TRANG CHECKOUT

## ✨ Tính năng mới

### 1. **Chỉnh sửa thời gian thuê**
- Click nút **"Chỉnh sửa"** bên cạnh "Thời gian thuê"
- Thay đổi ngày/giờ nhận xe và trả xe
- Click **"Lưu thay đổi"** 
- Giá sẽ tự động cập nhật theo số ngày mới

### 2. **Thêm/Xóa mã giảm giá**

#### Thêm mã giảm giá:
- Nhập mã vào ô "Nhập mã giảm giá"
- Click **"Áp dụng"**
- Nếu hợp lệ → Hiển thị số tiền giảm

#### Xóa mã giảm giá:
- Click nút **"Xóa"** bên cạnh mã đã áp dụng
- Xác nhận → Mã sẽ bị xóa và giá cập nhật

### 3. **Chọn phương thức thanh toán**
- 💵 **Tiền mặt**: Thanh toán khi nhận xe
- 🏦 **Chuyển khoản**: Hiển thị thông tin tài khoản
- 💳 **Thẻ tín dụng**: Chuyển đến cổng thanh toán

## 📝 Mã giảm giá có sẵn (để test)

```
NEWCUST10    - Giảm 10% (tối thiểu 500.000đ)
STUDENT12    - Giảm 12% cho sinh viên (tối thiểu 400.000đ)
FLASH100K    - Giảm 100.000đ (tối thiểu 1.000.000đ)
```

## 🚀 Luồng sử dụng

1. **Trang chi tiết xe** → Chọn ngày, áp dụng discount (tùy chọn)
2. Click **"CHỌN THUÊ"** → Chuyển sang trang Checkout
3. **Trang Checkout**:
   - ✏️ Chỉnh sửa thời gian (nếu cần)
   - 🏷️ Thêm/Xóa mã giảm giá
   - 💳 Chọn phương thức thanh toán
   - ✅ Đồng ý điều khoản
   - Click **"Xác nhận đặt xe"**
4. **Trang Confirmation** → Hoàn tất!

## 💡 Lưu ý

- Khi thay đổi thời gian hoặc discount, trang sẽ tự động reload để cập nhật giá chính xác
- Mã giảm giá phải đủ điều kiện:
  - Còn hiệu lực (trong thời gian StartDate - EndDate)
  - Đơn hàng đủ giá trị tối thiểu
  - Còn lượt sử dụng
  - Trạng thái Active

## 🎯 Ưu điểm

✅ Linh hoạt: Có thể sửa thời gian và discount ngay trên checkout
✅ Trực quan: Thấy ngay thay đổi giá khi sửa
✅ An toàn: Validate lại trên server trước khi lưu
✅ UX tốt: Không cần quay lại trang trước
