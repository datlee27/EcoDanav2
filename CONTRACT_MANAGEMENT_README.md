# Contract Management System

## Tổng quan
Hệ thống quản lý hợp đồng cho phép admin/staff quản lý các hợp đồng thuê xe điện.

## Các thành phần đã tạo

### 1. Backend Components

#### Model
- **Contract.java** (`src/main/java/com/ecodana/evodanavn1/model/Contract.java`)
  - Các trạng thái: Draft, Signed, Completed, Cancelled
  - Liên kết với User và Booking
  - Lưu thông tin chữ ký, ngày ký, ngày hoàn thành

#### Repository
- **ContractRepository.java** (`src/main/java/com/ecodana/evodanavn1/repository/ContractRepository.java`)
  - Các query methods để tìm kiếm contracts
  - Hỗ trợ filter theo status, user, booking
  - Search contracts theo nhiều tiêu chí

#### Service
- **ContractService.java** (`src/main/java/com/ecodana/evodanavn1/service/ContractService.java`)
  - Business logic cho contract management
  - Tự động generate contract code
  - Methods: create, update, sign, complete, cancel

#### Controller
- **ContractController.java** (`src/main/java/com/ecodana/evodanavn1/controller/admin/ContractController.java`)
  - REST API endpoints cho contract operations
  - Chỉ admin/staff mới có quyền truy cập

### 2. Frontend Components

#### View Template
- **admin-contracts.html** (`src/main/resources/templates/admin/admin-contracts.html`)
  - Hiển thị danh sách contracts với filter và search
  - Statistics cards (Total, Draft, Signed, Completed, Cancelled)
  - Modal để xem chi tiết contract
  - Actions: View, Sign, Complete, Cancel

## API Endpoints

### GET `/admin/contracts`
Hiển thị trang quản lý contracts
- Query params: `status`, `search`

### GET `/admin/contracts/{id}`
Lấy thông tin chi tiết một contract
- Response: JSON với contract details

### POST `/admin/contracts`
Tạo contract mới
- Body: `{ "userId": "...", "bookingId": "...", "notes": "..." }`

### PUT `/admin/contracts/{id}`
Cập nhật contract
- Body: `{ "notes": "...", "status": "..." }`

### POST `/admin/contracts/{id}/sign`
Ký contract (chuyển từ Draft → Signed)
- Body: `{ "signatureData": "...", "signatureMethod": "digital" }`

### POST `/admin/contracts/{id}/complete`
Hoàn thành contract (chuyển từ Signed → Completed)

### POST `/admin/contracts/{id}/cancel`
Hủy contract
- Body: `{ "reason": "..." }`

### DELETE `/admin/contracts/{id}`
Xóa contract (chỉ admin)

## Database Schema

```sql
CREATE TABLE Contract (
    ContractId VARCHAR(36) PRIMARY KEY,
    ContractCode VARCHAR(30) UNIQUE NOT NULL,
    UserId VARCHAR(36) NOT NULL,
    BookingId VARCHAR(36) NOT NULL,
    CreatedDate DATETIME NOT NULL,
    SignedDate DATETIME,
    CompletedDate DATETIME,
    Status VARCHAR(20) NOT NULL,
    TermsAccepted BOOLEAN NOT NULL DEFAULT FALSE,
    SignatureData TEXT,
    SignatureMethod VARCHAR(20),
    ContractPdfUrl VARCHAR(500),
    Notes VARCHAR(500),
    CancellationReason VARCHAR(500),
    CitizenIdSnapshotId VARCHAR(36),
    DriverLicenseSnapshotId VARCHAR(36),
    FOREIGN KEY (UserId) REFERENCES Users(UserId),
    FOREIGN KEY (BookingId) REFERENCES Booking(BookingId)
);
```

## Cách sử dụng

### 1. Khởi động ứng dụng
```bash
mvn spring-boot:run
```

### 2. Truy cập Contract Management
- URL: `http://localhost:8080/admin/contracts`
- Yêu cầu: Đăng nhập với tài khoản Admin hoặc Staff

### 3. Tạo sample data (optional)
Chạy file SQL: `src/main/resources/db/sample-contracts.sql`

### 4. Các chức năng chính

#### Xem danh sách contracts
- Filter theo status: All Status, Draft, Signed, Completed, Cancelled
- Search theo contract code, customer name, email

#### Xem chi tiết contract
- Click nút "View" để xem thông tin đầy đủ
- Hiển thị: Customer info, Vehicle info, Booking details, Dates, Status

#### Ký contract
- Chỉ áp dụng cho contracts có status = Draft
- Click nút "Sign" → Confirm → Contract chuyển sang Signed

#### Hoàn thành contract
- Chỉ áp dụng cho contracts có status = Signed
- Click nút "Complete" → Confirm → Contract chuyển sang Completed

#### Hủy contract
- Áp dụng cho contracts chưa Completed hoặc Cancelled
- Click nút "Cancel" → Nhập lý do → Contract chuyển sang Cancelled

## Contract Lifecycle

```
Draft → Signed → Completed
  ↓       ↓
  Cancelled
```

1. **Draft**: Contract mới tạo, chưa được ký
2. **Signed**: Contract đã được ký, đang chờ hoàn thành
3. **Completed**: Contract đã hoàn thành
4. **Cancelled**: Contract bị hủy (có thể hủy từ Draft hoặc Signed)

## Features

✅ Quản lý contracts với CRUD operations
✅ Filter và search contracts
✅ Statistics dashboard
✅ Contract lifecycle management
✅ Automatic contract code generation
✅ Role-based access control (Admin/Staff only)
✅ Modal view cho contract details
✅ Responsive design với TailwindCSS

## Lưu ý

1. **Quyền truy cập**: Chỉ Admin và Staff mới có quyền truy cập Contract Management
2. **Delete**: Chỉ Admin mới có quyền xóa contracts
3. **Contract Code**: Tự động generate theo format `CT-YYYYMMDDHHMMSS-XXXXXX`
4. **Validation**: Kiểm tra user và booking tồn tại trước khi tạo contract

## Troubleshooting

### Lỗi "No contracts found"
- Kiểm tra database có contracts không
- Chạy file `sample-contracts.sql` để tạo test data

### Lỗi "Access denied"
- Đảm bảo đã đăng nhập với tài khoản Admin/Staff
- Kiểm tra role trong database

### Lỗi khi sign/complete/cancel
- Kiểm tra contract status có đúng không
- Xem console log để biết chi tiết lỗi

## Next Steps (Tùy chọn mở rộng)

- [ ] Export contract to PDF
- [ ] Email notification khi contract status thay đổi
- [ ] Upload và lưu trữ chữ ký điện tử
- [ ] Contract templates
- [ ] Contract versioning
- [ ] Advanced search và filters
- [ ] Pagination cho danh sách contracts
