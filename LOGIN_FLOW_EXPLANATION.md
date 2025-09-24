# Luồng Đăng Nhập và Phân Quyền Dashboard

## 🔄 Luồng Hoạt Động Chi Tiết

### 1. Đăng Nhập (Login Flow)

```
User nhập thông tin → AuthController.doLogin() → Kiểm tra thông tin → Load User với Role → Lưu vào Session → Redirect
```

**Chi tiết từng bước:**

1. **User nhập thông tin đăng nhập**
   - Username/Email: `customer@test.com`
   - Password: `password123`

2. **AuthController.doLogin() xử lý:**
   ```java
   // Tìm user theo username hoặc email
   User user = userService.findByUsername(username);
   if (user == null) {
       user = userService.findByEmail(username);
   }
   ```

3. **Xác thực mật khẩu:**
   ```java
   if (user != null && userService.login(user.getEmail(), password, null) != null) {
       // Mật khẩu đúng, tiếp tục
   }
   ```

4. **Load User với Role từ Database:**
   ```java
   // Lấy user với thông tin role đầy đủ từ database
   User userWithRole = userService.getUserWithRole(user.getEmail());
   ```

5. **Lưu User vào Session:**
   ```java
   // Lưu user (có role) vào session
   session.setAttribute("currentUser", userWithRole);
   ```

6. **Redirect đến Dashboard Router:**
   ```java
   return "redirect:/dashboard-router";
   ```

### 2. Dashboard Routing (Phân Quyền)

```
DashboardRouterController → Lấy User từ Session → Kiểm tra Role → Redirect đến Dashboard phù hợp
```

**Chi tiết từng bước:**

1. **DashboardRouterController.dashboardRouter() nhận request:**
   ```java
   @GetMapping("/dashboard-router")
   public String dashboardRouter(HttpSession session, RedirectAttributes redirectAttributes) {
   ```

2. **Lấy User từ Session:**
   ```java
   User currentUser = (User) session.getAttribute("currentUser");
   ```

3. **Reload User với Role mới nhất từ Database:**
   ```java
   // Đảm bảo role được load đầy đủ từ database
   User userWithRole = userService.getUserWithRole(currentUser.getEmail());
   ```

4. **Cập nhật Session với dữ liệu mới:**
   ```java
   session.setAttribute("currentUser", userWithRole);
   ```

5. **Kiểm tra Role và Redirect:**
   ```java
   if (userService.isAdmin(userWithRole)) {
       return "redirect:/admin";           // Admin Dashboard
   } else if (userService.isStaff(userWithRole)) {
       return "redirect:/staff";           // Staff Dashboard  
   } else if (userService.isCustomer(userWithRole)) {
       return "redirect:/dashboard";       // Customer Dashboard
   } else {
       return "redirect:/dashboard";       // Default Customer
   }
   ```

### 3. Role Checking Logic

**UserService.isAdmin(user):**
```java
public boolean isAdmin(User user) {
    return hasRole(user, "Admin");
}

private boolean hasRole(User user, String roleName) {
    if (user == null || user.getRole() == null) {
        return false;
    }
    return roleName.equalsIgnoreCase(user.getRole().getRoleName());
}
```

**UserService.isStaff(user):**
```java
public boolean isStaff(User user) {
    return hasRole(user, "Staff");
}
```

**UserService.isCustomer(user):**
```java
public boolean isCustomer(User user) {
    return hasRole(user, "Customer");
}
```

### 4. Database Schema

**Bảng Users:**
```sql
CREATE TABLE `Users` (
  `UserId` char(36) NOT NULL,
  `RoleId` char(36) NOT NULL,  -- Foreign key đến Roles
  `Username` varchar(100) NOT NULL,
  `Email` varchar(100) NOT NULL,
  -- ... other fields
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`RoleId`) REFERENCES `Roles` (`RoleId`)
);
```

**Bảng Roles:**
```sql
CREATE TABLE `Roles` (
  `RoleId` char(36) NOT NULL,
  `RoleName` varchar(50) DEFAULT NULL,  -- 'Customer', 'Staff', 'Admin'
  `NormalizedName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`RoleId`)
);
```

**Dữ liệu mẫu:**
```sql
INSERT INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('customer-role-id', 'Customer', 'CUSTOMER'),
('staff-role-id', 'Staff', 'STAFF'),
('admin-role-id', 'Admin', 'ADMIN');
```

### 5. Session Data Structure

**Session chứa:**
```java
session.setAttribute("currentUser", userWithRole);
```

**User object trong session có:**
- `userId`: ID của user
- `roleId`: ID của role (foreign key)
- `role`: Role object với đầy đủ thông tin
  - `role.roleId`: ID của role
  - `role.roleName`: Tên role ("Customer", "Staff", "Admin")
  - `role.normalizedName`: Tên chuẩn hóa ("CUSTOMER", "STAFF", "ADMIN")

### 6. Dashboard Mapping

| Role | Route | Template | Controller Method |
|------|-------|----------|-------------------|
| Customer | `/dashboard` | `customer/customer-dashboard.html` | `DashboardController.dashboard()` |
| Staff | `/staff` | `staff/staff-dashboard.html` | `DashboardController.staff()` |
| Admin | `/admin` | `admin/admin-dashboard.html` | `DashboardController.admin()` |

### 7. Luồng Hoàn Chỉnh

```
1. User Login
   ↓
2. AuthController.doLogin()
   ↓
3. UserService.login() - Xác thực
   ↓
4. UserService.getUserWithRole() - Load role từ DB
   ↓
5. session.setAttribute("currentUser", userWithRole)
   ↓
6. redirect:/dashboard-router
   ↓
7. DashboardRouterController.dashboardRouter()
   ↓
8. Lấy user từ session
   ↓
9. UserService.getUserWithRole() - Reload role
   ↓
10. Kiểm tra role:
    - isAdmin() → redirect:/admin
    - isStaff() → redirect:/staff  
    - isCustomer() → redirect:/dashboard
   ↓
11. DashboardController tương ứng
   ↓
12. Load dữ liệu theo role
   ↓
13. Render template phù hợp
```

### 8. Ưu Điểm Của Luồng Này

✅ **Bảo mật**: Role được load từ database, không hardcode
✅ **Linh hoạt**: Dễ thêm role mới
✅ **Nhất quán**: Luôn có dữ liệu role mới nhất
✅ **Tách biệt**: Logic routing tách riêng với logic dashboard
✅ **Kiểm tra**: Mỗi dashboard kiểm tra quyền truy cập
✅ **Session**: Dữ liệu user và role được lưu trong session

### 9. Test Cases

**Test Customer Login:**
1. Login với `customer@test.com` / `password123`
2. Kiểm tra session có `currentUser` với `role.roleName = "Customer"`
3. Redirect đến `/dashboard` (Customer Dashboard)

**Test Staff Login:**
1. Login với `staff@test.com` / `password123`
2. Kiểm tra session có `currentUser` với `role.roleName = "Staff"`
3. Redirect đến `/staff` (Staff Dashboard)

**Test Admin Login:**
1. Login với `admin@test.com` / `password123`
2. Kiểm tra session có `currentUser` với `role.roleName = "Admin"`
3. Redirect đến `/admin` (Admin Dashboard)
