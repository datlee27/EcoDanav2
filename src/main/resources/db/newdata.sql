

INSERT INTO Roles (RoleId, RoleName, NormalizedName)
VALUES ('550e8400-e29b-41d4-a716-446655440001', 'Admin', 'ADMIN'),
       ('550e8400-e29b-41d4-a716-446655440002', 'Staff', 'STAFF'),
       ('550e8400-e29b-41d4-a716-446655440003', 'Owner', 'OWNER'),
       ('550e8400-e29b-41d4-a716-446655440004', 'Customer', 'CUSTOMER');

INSERT INTO Users (UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName, Status, RoleId, CreatedDate,
                   NormalizedUserName, NormalizedEmail, EmailVerifed, SecurityStamp, ConcurrencyStamp, TwoFactorEnabled,
                   LockoutEnabled, AccessFailedCount)
VALUES ('550e8400-e29b-41d4-a716-446655440100', 'admin', 'admin@ecodana.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0123456789', 'Admin', 'EvoDana', 'Active',
        '550e8400-e29b-41d4-a716-446655440001', NOW(), 'ADMIN', 'ADMIN@ECODANA.COM', 1, 'admin-security-stamp',
        'admin-concurrency-stamp', 0, 0, 0),
       ('550e8400-e29b-41d4-a716-446655440101', 'staff', 'staff@ecodana.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0987654321', 'Staff', 'EvoDana', 'Active',
        '550e8400-e29b-41d4-a716-446655440002', NOW(), 'STAFF', 'STAFF@ECODANA.COM', 1, 'staff-security-stamp',
        'staff-concurrency-stamp', 0, 0, 0),
       ('550e8400-e29b-41d4-a716-446655440102', 'owner', 'owner@ecodana.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0912345678', 'Owner', 'EvoDana', 'Active',
        '550e8400-e29b-41d4-a716-446655440003', NOW(), 'OWNER', 'OWNER@ECODANA.COM', 1, 'owner-security-stamp',
        'owner-concurrency-stamp', 0, 0, 0),
       ('550e8400-e29b-41d4-a716-446655440103', 'customer', 'customer@ecodana.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901234567', 'Customer', 'EvoDana', 'Active',
        '550e8400-e29b-41d4-a716-446655440004', NOW(), 'CUSTOMER', 'CUSTOMER@ECODANA.COM', 1, 'customer-security-stamp',
        'customer-concurrency-stamp', 0, 0, 0);



-- pass all :  password


-- =============================================
-- SECTION 1: DỮ LIỆU BẢNG TRA CỨU
-- =============================================
-- use
-- ecodanav2;

-- Thêm dữ liệu cho loại hộp số
INSERT INTO `TransmissionTypes` (`TransmissionTypeId`, `TransmissionTypeName`)
VALUES (1, 'Automatic');

-- Thêm dữ liệu cho các danh mục xe
INSERT INTO `VehicleCategories` (`CategoryId`, `CategoryName`)
VALUES
    (1, 'Electric Car'), (2, 'Electric Motorbike');


-- =============================================
-- SECTION 2: DỮ LIỆU XE Ô TÔ ĐIỆN (ElectricCar)
-- =============================================

-- Mẫu 1: VinFast VF8 Eco
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0001-4a5b-8c9d-111111111111', 'VinFast VF8 Eco', 2023, '43A-123.45', 5, 15000,
        '{"hourly": 150000, "daily": 1200000, "monthly": 25000000}',
        'Available',
        'SUV điện 5 chỗ rộng rãi, phù hợp cho gia đình du lịch Đà Nẵng. Phạm vi di chuyển lên tới 420km mỗi lần sạc.',
        'ElectricCar', 1, 82.00, NOW(),
        1, 1, 'https://example.com/images/vf8-main.jpg',
        '["https://example.com/images/vf8-1.jpg", "https://example.com/images/vf8-2.jpg", "https://example.com/images/vf8-3.jpg"]',
        '["GPS", "Camera 360", "Cửa sổ trời", "Sạc không dây", "ADAS"]');

-- Mẫu 2: Tesla Model 3
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0002-4a5b-8c9d-222222222222', 'Tesla Model 3', 2022, '43A-567.89', 5, 22000,
        '{"hourly": 200000, "daily": 1800000, "monthly": 35000000}',
        'Available',
        'Trải nghiệm công nghệ xe điện hàng đầu thế giới. Thiết kế tối giản, hiệu suất cao và hệ thống Autopilot thông minh.',
        'ElectricCar', 1, 60.00, NOW(),
        2, 1, 'https://example.com/images/model3-main.jpg',
        '["https://example.com/images/model3-1.jpg", "https://example.com/images/model3-2.jpg"]',
        '["GPS", "Autopilot", "Màn hình cảm ứng lớn", "Sưởi ghế", "Bluetooth"]');

-- Mẫu 3: Hyundai Ioniq 5
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0003-4a5b-8c9d-333333333333', 'Hyundai Ioniq 5', 2023, '43B-111.22', 5, 8000,
        '{"hourly": 160000, "daily": 1400000, "monthly": 28000000}',
        'Maintenance',
        'Chiếc crossover điện với thiết kế độc đáo, không gian nội thất linh hoạt và khả năng sạc siêu nhanh.',
        'ElectricCar', 1, 72.60, NOW(),
        3, 1, 'https://example.com/images/ioniq5-main.jpg',
        '["https://example.com/images/ioniq5-1.jpg", "https://example.com/images/ioniq5-2.jpg"]',
        '["GPS", "Sạc V2L", "Ghế thư giãn", "Màn hình kép", "Hỗ trợ giữ làn"]');



-- =============================================
-- SECTION 3: DỮ LIỆU XE MÁY ĐIỆN (ElectricMotorcycle)
-- =============================================

-- Mẫu 1: VinFast Feliz S
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0004-4a5b-8c9d-444444444444', 'VinFast Feliz S', 2023, '43-C1-123.45', 2, 5000,
        '{"hourly": 20000, "daily": 150000, "monthly": 2500000}',
        'Available',
        'Xe máy điện nhỏ gọn, cốp rộng, phù hợp để dạo quanh thành phố Đà Nẵng. Quãng đường di chuyển ~198km.',
        'ElectricMotorcycle', 1, 3.50, NOW(),
        4, 1, 'https://example.com/images/feliz-s-main.jpg',
        '["https://example.com/images/feliz-s-1.jpg"]',
        '["Đèn LED", "Cốp rộng", "Chìa khóa thông minh"]');

-- Mẫu 2: Dat Bike Weaver 200
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0005-4a5b-8c9d-555555555555', 'Dat Bike Weaver 200', 2022, '43-D1-567.89', 2, 8500,
        '{"hourly": 25000, "daily": 180000, "monthly": 3000000}',
        'Rented',
        'Xe máy điện "made in Vietnam" với khả năng tăng tốc mạnh mẽ và phạm vi hoạt động 200km. Sạc nhanh trong 3 giờ.',
        'ElectricMotorcycle', 1, 4.10, NOW(),
        5, 1, 'https://example.com/images/weaver200-main.jpg',
        '["https://example.com/images/weaver200-1.jpg"]',
        '["Phanh động cơ", "Sạc nhanh", "Thiết kế cổ điển"]');

-- Mẫu 3: Yadea G5
INSERT INTO `Vehicle`
(`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`,
 `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`,
 `MainImageUrl`, `ImageUrls`, `Features`)
VALUES ('a1b2c3d4-0006-4a5b-8c9d-666666666666', 'Yadea G5', 2023, '43-E1-987.65', 2, 2100,
        '{"hourly": 18000, "daily": 140000, "monthly": 2200000}',
        'Available',
        'Xe tay ga điện với thiết kế hiện đại, màn hình LCD lớn và hệ thống đèn full LED. Vận hành êm ái, nhẹ nhàng.',
        'ElectricMotorcycle', 0, 1.87, NOW(),
        4, 1, 'https://example.com/images/yadea-g5-main.jpg',
        '["https://example.com/images/yadea-g5-1.jpg"]',
        '["Màn hình LCD", "Đèn LED toàn xe", "Hệ thống định vị"]');