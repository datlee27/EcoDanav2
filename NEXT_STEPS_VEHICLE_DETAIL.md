# Các Bước Tiếp Theo - Cập Nhật Trang Chi Tiết Xe

## ✅ Đã Hoàn Thành

1. **Phân tích giao diện Mioto** - Tạo file `VEHICLE_DETAIL_MIOTO_STYLE.md`
2. **Cập nhật header** - Title + Rating + Location
3. **Cập nhật image gallery** - Grid 3+1 columns theo style Mioto

## 🔄 Đang Thực Hiện

### Cần thay thế phần Vehicle Details bằng Tab Navigation

**Vị trí:** Sau image gallery, thay thế toàn bộ phần từ line 109 đến line 200+

**Cấu trúc mới:**

```html
<!-- Tab Navigation Container -->
<div class="bg-white rounded-lg shadow-md mb-6">
    <!-- Tab Buttons -->
    <div class="flex border-b border-gray-200 overflow-x-auto">
        <button class="tab-button active" data-tab="features">Đặc điểm</button>
        <button class="tab-button" data-tab="documents">Giấy tờ thuê xe</button>
        <button class="tab-button" data-tab="location">Vị trí xe</button>
        <button class="tab-button" data-tab="owner">Chủ xe</button>
    </div>

    <!-- Tab Contents -->
    <div class="p-6">
        <!-- Tab 1: Đặc điểm -->
        <div id="features" class="tab-content active">
            <!-- Specs grid 4 cols -->
            <!-- Description -->
            <!-- Features list -->
        </div>

        <!-- Tab 2: Giấy tờ thuê xe -->
        <div id="documents" class="tab-content">
            <!-- 2 options -->
            <!-- Tài sản thế chấp -->
            <!-- Điều khoản -->
        </div>

        <!-- Tab 3: Vị trí xe -->
        <div id="location" class="tab-content">
            <!-- Address -->
            <!-- Map -->
        </div>

        <!-- Tab 4: Chủ xe -->
        <div id="owner" class="tab-content">
            <!-- Owner card -->
            <!-- Reviews -->
        </div>
    </div>
</div>
```

## 📝 Chi Tiết Từng Tab

### Tab 1: Đặc điểm (Features)

```html
<div id="features" class="tab-content active">
    <h2 class="text-xl font-semibold mb-4">Đặc điểm</h2>
    
    <!-- Key Specs Grid -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
            <i class="fas fa-cog text-2xl text-green-500"></i>
            <div>
                <p class="text-xs text-gray-600">Truyền động</p>
                <p class="font-semibold">Số tự động</p>
            </div>
        </div>
        <!-- Repeat for: Số ghế, Nhiên liệu, Tiêu hao -->
    </div>

    <!-- Description -->
    <div class="mb-6">
        <h3 class="font-semibold mb-3">Mô tả</h3>
        <p class="text-gray-700 leading-relaxed">...</p>
    </div>

    <!-- Features -->
    <div>
        <h3 class="font-semibold mb-3">Các tiện nghi khác</h3>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
            <div class="flex items-center gap-2 p-2 bg-gray-50 rounded-lg">
                <i class="fas fa-check-circle text-green-500"></i>
                <span class="text-sm">Bluetooth</span>
            </div>
            <!-- Repeat for all features -->
        </div>
    </div>
</div>
```

### Tab 2: Giấy tờ thuê xe (Documents)

```html
<div id="documents" class="tab-content">
    <h2 class="text-xl font-semibold mb-4">Giấy tờ thuê xe</h2>
    
    <!-- Warning Box -->
    <div class="bg-orange-50 border-l-4 border-orange-500 p-4 mb-6">
        <p class="text-sm text-orange-800">
            <i class="fas fa-exclamation-circle mr-2"></i>
            Chọn 1 trong 2 hình thức
        </p>
    </div>

    <!-- Options -->
    <div class="space-y-4">
        <!-- Option 1 -->
        <div class="border-2 border-gray-200 rounded-lg p-4 hover:border-green-500 transition-all cursor-pointer">
            <div class="flex items-start gap-3">
                <i class="fas fa-id-card text-2xl text-green-500 mt-1"></i>
                <div class="flex-1">
                    <h4 class="font-semibold mb-2">GPLX (đối chiếu) & Passport (giữ lại)</h4>
                    <p class="text-sm text-gray-600">...</p>
                </div>
            </div>
        </div>

        <!-- Option 2 -->
        <div class="border-2 border-gray-200 rounded-lg p-4 hover:border-green-500 transition-all cursor-pointer">
            <div class="flex items-start gap-3">
                <i class="fas fa-credit-card text-2xl text-green-500 mt-1"></i>
                <div class="flex-1">
                    <h4 class="font-semibold mb-2">GPLX (đối chiếu) & CCCD (đối chiếu VNeID)</h4>
                    <p class="text-sm text-gray-600">...</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Tài sản thế chấp -->
    <div class="mt-8">
        <h3 class="font-semibold mb-4">Tài sản thế chấp</h3>
        <p class="text-sm text-gray-700 mb-4">15 triệu (Tiền mặt/Chuyển khoản...)</p>
    </div>

    <!-- Điều khoản -->
    <div class="mt-6">
        <h3 class="font-semibold mb-4">Điều khoản</h3>
        <ul class="space-y-2 text-sm text-gray-700">
            <li class="flex items-start gap-2">
                <i class="fas fa-check text-green-500 mt-1"></i>
                <span>Sử dụng xe đúng mục đích</span>
            </li>
            <!-- More terms... -->
        </ul>
    </div>
</div>
```

### Tab 3: Vị trí xe (Location)

```html
<div id="location" class="tab-content">
    <h2 class="text-xl font-semibold mb-4">Vị trí xe</h2>
    
    <div class="mb-4">
        <div class="flex items-start gap-3">
            <i class="fas fa-map-marker-alt text-green-500 text-xl mt-1"></i>
            <div>
                <p class="font-semibold">Phường 17, Quận Bình Thạnh</p>
                <p class="text-sm text-gray-600">Đây chỉ là khu vực gần đúng...</p>
            </div>
        </div>
    </div>

    <!-- Map Placeholder -->
    <div class="bg-gray-200 rounded-lg h-64 flex items-center justify-center">
        <div class="text-center text-gray-500">
            <i class="fas fa-map text-4xl mb-2"></i>
            <p>Bản đồ sẽ được hiển thị tại đây</p>
        </div>
    </div>

    <div class="mt-4 text-right">
        <a href="#" class="text-green-500 hover:text-green-600 font-semibold">
            Xem bản đồ <i class="fas fa-arrow-right ml-1"></i>
        </a>
    </div>
</div>
```

### Tab 4: Chủ xe (Owner)

```html
<div id="owner" class="tab-content">
    <h2 class="text-xl font-semibold mb-4">Chủ xe</h2>
    
    <!-- Owner Info Card -->
    <div class="bg-gray-50 rounded-lg p-6 mb-6">
        <div class="flex items-center gap-4 mb-4">
            <img src="https://via.placeholder.com/80" alt="Owner" class="w-20 h-20 rounded-full">
            <div class="flex-1">
                <h3 class="text-xl font-semibold">Lê Xuân An</h3>
                <div class="flex items-center gap-2 text-sm text-gray-600 mt-1">
                    <i class="fas fa-star text-yellow-400"></i>
                    <span class="font-semibold">5.0</span>
                    <span>•</span>
                    <span>18 chuyến</span>
                </div>
            </div>
        </div>

        <!-- Owner Stats -->
        <div class="grid grid-cols-3 gap-4 text-center">
            <div class="p-3 bg-white rounded-lg">
                <p class="text-2xl font-bold text-green-500">100%</p>
                <p class="text-xs text-gray-600">Tỉ lệ phản hồi</p>
            </div>
            <!-- More stats... -->
        </div>
    </div>

    <!-- Reviews -->
    <div>
        <h3 class="font-semibold mb-4">
            <i class="fas fa-star text-yellow-400 mr-2"></i>
            5.0 • 13 đánh giá
        </h3>

        <div class="space-y-4">
            <!-- Review items... -->
        </div>

        <button class="mt-4 w-full py-2 border border-green-500 text-green-500 rounded-lg hover:bg-green-50">
            Xem thêm
        </button>
    </div>
</div>
```

## 🎨 Cập Nhật Sidebar Booking

### Thay đổi cần thiết:

1. **Giá lớn hơn** - 2rem font-size
2. **Thêm insurance badges:**
   - Badge xanh: Bảo hiểm thuê xe
   - Badge đỏ HOT: Bảo hiểm người trên xe
3. **Form updates:**
   - Thêm dropdown địa điểm giao xe
   - Grid 2 cols cho date/time
4. **Price breakdown:**
   - Đơn giá thuê
   - Bảo hiểm thuê xe
   - Bảo hiểm bổ sung (checkbox)
   - Phí phụ (collapsible)
   - Tổng cộng (lớn, bold)

```html
<!-- Pricing -->
<div class="mb-6">
    <div class="flex items-baseline gap-2 mb-4">
        <span class="text-3xl font-bold text-green-500">1.002K</span>
        <span class="text-gray-600">/ngày</span>
    </div>

    <!-- Insurance Badge Green -->
    <div class="bg-green-50 border border-green-200 rounded-lg p-3 mb-4">
        <div class="flex items-center gap-2">
            <i class="fas fa-shield-alt text-green-500"></i>
            <div class="flex-1">
                <p class="text-sm font-semibold text-green-700">Bảo hiểm thuê xe</p>
                <p class="text-xs text-gray-600">Chuyến đi có mua bảo hiểm...</p>
            </div>
        </div>
        <a href="#" class="text-xs text-green-600 hover:underline mt-2 inline-block">Xem thêm ›</a>
    </div>

    <!-- Insurance Badge Red HOT -->
    <div class="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
        <div class="flex items-center gap-2">
            <span class="bg-red-500 text-white text-xs px-2 py-1 rounded">HOT</span>
            <div class="flex-1">
                <p class="text-sm font-semibold">Bảo hiểm người trên xe</p>
                <p class="text-lg font-bold text-green-500">50.000đ/ngày</p>
            </div>
        </div>
        <p class="text-xs text-gray-600 mt-2">Trường hợp xảy ra tai nạn...</p>
    </div>
</div>
```

## 🔧 JavaScript Updates

### Tab Switching

```javascript
document.querySelectorAll('.tab-button').forEach(button => {
    button.addEventListener('click', () => {
        // Remove active from all
        document.querySelectorAll('.tab-button').forEach(b => 
            b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => 
            c.classList.remove('active'));
        
        // Add active to clicked
        button.classList.add('active');
        const tabId = button.getAttribute('data-tab');
        document.getElementById(tabId).classList.add('active');
    });
});
```

### Image Gallery Update

```javascript
document.querySelectorAll('.thumbnail-item').forEach((thumb, index) => {
    thumb.addEventListener('click', () => {
        const mainImage = document.getElementById('mainImage');
        const img = thumb.querySelector('img');
        mainImage.src = img.src;
        
        // Update active thumbnail
        document.querySelectorAll('.thumbnail-item').forEach(t => {
            t.classList.remove('active', 'border-green-500');
            t.classList.add('border-gray-200');
        });
        thumb.classList.remove('border-gray-200');
        thumb.classList.add('active', 'border-green-500');
    });
});
```

## 📋 Checklist Implementation

- [x] Cập nhật header với title + rating
- [x] Cập nhật image gallery (grid 3+1)
- [ ] Thay thế vehicle details bằng tab navigation
- [ ] Implement tab 1: Đặc điểm
- [ ] Implement tab 2: Giấy tờ thuê xe
- [ ] Implement tab 3: Vị trí xe
- [ ] Implement tab 4: Chủ xe
- [ ] Cập nhật sidebar booking với insurance badges
- [ ] Cập nhật form với địa điểm giao xe
- [ ] Thêm price breakdown chi tiết
- [ ] Update JavaScript cho tabs
- [ ] Update JavaScript cho image gallery
- [ ] Test responsive design
- [ ] Test all interactions

## 🚀 Lệnh Chạy Test

```bash
# Compile
mvn clean compile

# Run
mvn spring-boot:run

# Access
http://localhost:8080/vehicles/{vehicleId}
```

## 📝 Notes

- File backup đã tạo: `vehicle-detail-backup.html`
- Tài liệu chi tiết: `VEHICLE_DETAIL_MIOTO_STYLE.md`
- CSS cần cập nhật trong `vehicle-detail.css`
- Màu chính: #10b981 (green)
- Font: Poppins

---

**Status:** 🔄 IN PROGRESS  
**Next:** Replace vehicle details section with tabs  
**Priority:** HIGH
