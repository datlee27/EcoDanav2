# 🚀 Git Workflow cho Team EcoDana_v1.1

| Role | Email | Password   | Dashboard |
|------|------|------------|-----|
| **Admin** | `admin@ecodana.com` | `password` | `/admin` |
| **Owner** | `owner@ecodana.com` | `password` | `/owner/dashboard` |
| **Customer** | `customer@ecodana.com` | `password` | `/` |





## 🚀 Deployment

### Production Setup
1. Configure production database
2. Update `application.properties`
3. Set environment variables
4. Build and deploy JAR file

## 🚀 Quick Start

### 1. Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

## 🏗️ Architecture

### Backend
- **Spring Boot 3.x** - Main framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **MySQL** - Database
- **OAuth2** - Google login support

### Frontend
- **Thymeleaf** - Template engine
- **Tailwind CSS** - Styling
- **Font Awesome** - Icons
- **JavaScript** - Interactive features

## 🔐 Authentication System

### Roles
- **Admin:** Full system access, user management
- **Staff/Owner:** Vehicle management, booking management
- **Customer:** Browse vehicles, make bookings

---

## 3️⃣ Checkout sang nhánh của mình
👉 Mỗi người có **nhánh riêng** để code (do lead tạo sẵn).

- **Ky:**
  ```bash
  git checkout ky
  ```
- **Uyen:**
  ```bash
  git checkout uyen
  ```
- **Nguyen:**
  ```bash
  git checkout nguyen
  ```

📌 **Kiểm tra nhánh hiện tại:**
```bash
git branch
# * ký hiệu nhánh đang ở
```

---

## 4️⃣ Nếu bạn muốn reset nhánh giống hệt main
⚠️ **Cẩn thận**: Sẽ mất hết thay đổi chưa commit!
```bash
git checkout <ten_nhanh>
git reset --hard origin/main
git push origin <ten_nhanh> --force
```

---

## 5️⃣ Cập nhật code mới nhất từ GitHub (trước khi code)
```bash
# Pull code mới nhất từ main
git pull origin main

# Hoặc pull từ nhánh dev nếu team đang dùng
git pull origin dev
```

---

## 6️⃣ Quy trình làm việc hằng ngày

### 🔄 Trước khi bắt đầu code:
```bash
# 1. Đảm bảo ở nhánh của mình
git checkout <ten_nhanh_cua_minh>

# 2. Pull code mới nhất
git pull origin main

# 3. Kiểm tra ứng dụng chạy OK
mvn spring-boot:run
```

### 💻 Trong lúc code:
```bash
# 1. Code / chỉnh sửa files

# 2. Kiểm tra thay đổi
git status

# 3. Xem chi tiết thay đổi (optional)
git diff

# 4. Add & Commit thường xuyên
git add .
git commit -m "Mô tả ngắn gọn thay đổi"

# Ví dụ commit messages tốt:
# git commit -m "Add user authentication feature"
# git commit -m "Fix database connection issue"  
# git commit -m "Update UI for dashboard page"
```

### 📤 Kết thúc ngày làm việc:
```bash
# Push code lên nhánh của mình
git push origin <tên_nhánh_của_mình>
```

**Ví dụ của từng người:**
```bash
# Ky:
git push origin ky

# Uyen:  
git push origin uyen

# Nguyen:
git push origin nguyen
```

---

## 7️⃣ Merge code (chỉ lead làm)

### Merge vào nhánh dev:
```bash
# Checkout sang dev
git checkout dev
git pull origin dev

# Merge từ nhánh thành viên
git merge origin/ky     # hoặc uyen, nguyen
git push origin dev

# Test ứng dụng
mvn spring-boot:run
```

### Merge vào main (khi dev ổn định):
```bash
git checkout main
git pull origin main
git merge dev
git push origin main
```

---

## 8️⃣ Xử lý conflict (khi có xung đột)

### Khi pull bị conflict:
```bash
# Git sẽ báo conflict
git status
# sẽ thấy: both modified: filename.java

# Mở file conflict, tìm dòng:
# <<<<<<< HEAD
# your changes
# =======
# incoming changes  
# >>>>>>> branch_name

# Chỉnh sửa code, xóa các ký hiệu conflict

# Add & commit
git add .
git commit -m "Resolve merge conflict in filename.java"
```

---

## ✅ Nguyên tắc VÀNG

### 🚫 KHÔNG BAO GIỜ:
- Push trực tiếp vào `main` hoặc `dev`
- Commit file `.env` (chứa password)
- Force push khi có người khác đang làm việc
- Code mà không pull code mới

### ✅ LUÔN LUÔN:
- `git pull` trước khi code
- Commit thường xuyên với message rõ ràng
- Test ứng dụng trước khi push
- Backup code quan trọng

### 📝 Commit message tốt:
```bash
# ✅ TốT
git commit -m "Add login validation for user authentication"
git commit -m "Fix NPE in UserService.findById method"
git commit -m "Update database schema for new user fields"

# ❌ KHÔNG TỐT  
git commit -m "fix bug"
git commit -m "update"
git commit -m "asdfgh"
```

---

## 🆘 Troubleshooting

### Lỗi thường gặp:

**1. Database connection failed:**
```bash
# Kiểm tra MySQL đang chạy
sudo systemctl status mysql

# Kiểm tra .env có đúng không
cat .env
```

**2. Port 8080 already in use:**
```bash
# Kill process
lsof -ti:8080 | xargs kill -9

# Hoặc đổi port trong .env
APP_PORT=8081
```

**3. Git conflict:**
```bash
# Reset về trạng thái trước conflict
git reset --hard HEAD

# Pull lại
git pull origin main
```

**4. Quên nhánh nào đang làm:**
```bash
# Xem nhánh hiện tại
git branch

# Xem lịch sử commit
git log --oneline -5
```

---

## 📞 Liên hệ Support

- **Lead**: Dat Le - datlee27
- **Git Issues**: Tạo issue trên GitHub
- **Technical Problems**: Hỏi trong group chat

---

**🎯 Mục tiêu**: Code sạch, làm việc hiệu quả, ít conflict, deploy thành công!

**💪 Chúc team coding vui vẻ!** 🚀
