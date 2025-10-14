    USE ecodanangv2;

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
    -- DATABASE: ecodanav2
    -- NỘI DUNG: DỮ LIỆU MẪU XE ĐIỆN VINFAST (TỔNG HỢP)
    -- PHIÊN BẢN: HOÀN CHỈNH
    -- =============================================


    -- =============================================
    -- SECTION 1: DỮ LIỆU BẢNG TRA CỨU
    -- =============================================

    -- Thêm dữ liệu cho loại hộp số
    INSERT INTO `TransmissionTypes` (`TransmissionTypeId`, `TransmissionTypeName`)
    VALUES
        (1, 'Automatic')
        ON DUPLICATE KEY UPDATE TransmissionTypeName=VALUES(TransmissionTypeName);

    -- Thêm dữ liệu cho các danh mục xe
    INSERT INTO `VehicleCategories` (`CategoryId`, `CategoryName`)
    VALUES
        (1, 'Electric Car'),
        (2, 'Electric Motorbike')
        ON DUPLICATE KEY UPDATE CategoryName=VALUES(CategoryName);


    -- =============================================
    -- SECTION 2: DỮ LIỆU CÁC MẪU XE Ô TÔ ĐIỆN CỦA VINFAST
    -- =============================================

    -- Mẫu 1: VinFast VF 3
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0001-vf3', 'VinFast VF 3', 2024, '43A-301.11', 4, 1500,
         '{"hourly": 90000, "daily": 800000, "monthly": 18000000}',
         'Available', 'Mẫu mini-eSUV nhỏ gọn, cá tính, lý tưởng để di chuyển linh hoạt trong thành phố Đà Nẵng. Phạm vi di chuyển khoảng 210km.', 'ElectricCar', 1, 37.23, NOW(),
         1, 1, 'https://example.com/images/vf3-main.jpg',
         '["https://example.com/images/vf3-1.jpg", "https://example.com/images/vf3-2.jpg"]',
         '["Màn hình cảm ứng 10 inch", "Kết nối Bluetooth", "Chìa khóa thông minh"]');

    -- Mẫu 2: VinFast VF 5 Plus
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0002-vf5', 'VinFast VF 5 Plus', 2023, '43A-302.22', 5, 8000,
         '{"hourly": 100000, "daily": 900000, "monthly": 20000000}',
         'Available', 'Mẫu A-SUV cỡ nhỏ, phù hợp cho gia đình trẻ hoặc nhóm bạn khám phá các cung đường ven biển. Phạm vi di chuyển hơn 300km.', 'ElectricCar', 1, 37.23, NOW(),
         1, 1, 'https://example.com/images/vf5-main.jpg',
         '["https://example.com/images/vf5-1.jpg", "https://example.com/images/vf5-2.jpg"]',
         '["GPS", "Hỗ trợ lái xe ADAS", "Màn hình 8 inch", "Cảm biến lùi"]');

    -- Mẫu 3: VinFast VF e34
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0003-e34', 'VinFast VF e34', 2022, '43A-303.33', 5, 25000,
         '{"hourly": 120000, "daily": 1000000, "monthly": 22000000}',
         'Rented', 'Chiếc C-SUV thuần điện đầu tiên của VinFast, vận hành êm ái, trang bị trợ lý ảo thông minh. Phạm vi di chuyển khoảng 285km.', 'ElectricCar', 1, 42.00, NOW(),
         1, 1, 'https://example.com/images/vfe34-main.jpg',
         '["https://example.com/images/vfe34-1.jpg", "https://example.com/images/vfe34-2.jpg"]',
         '["Trợ lý ảo Vivi", "Điều hòa tự động", "Camera lùi", "Cảnh báo chệch làn"]');

    -- Mẫu 4: VinFast VF 6 Plus
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0004-vf6', 'VinFast VF 6 Plus', 2023, '43A-304.44', 5, 11000,
         '{"hourly": 140000, "daily": 1100000, "monthly": 24000000}',
         'Available', 'Mẫu B-SUV với thiết kế hiện đại từ studio Torino Design. Nội thất rộng rãi, phạm vi di chuyển lên tới 381km.', 'ElectricCar', 1, 59.60, NOW(),
         1, 1, 'https://example.com/images/vf6-main.jpg',
         '["https://example.com/images/vf6-1.jpg", "https://example.com/images/vf6-2.jpg"]',
         '["Màn hình giải trí 12.9 inch", "ADAS", "Cửa sổ trời", "Sạc không dây"]');

    -- Mẫu 5: VinFast VF 7 Plus
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0005-vf7', 'VinFast VF 7 Plus', 2024, '43A-305.55', 5, 5500,
         '{"hourly": 160000, "daily": 1300000, "monthly": 27000000}',
         'Maintenance', 'Thiết kế C-SUV mang phong cách tương lai, hiệu suất vận hành mạnh mẽ. Phạm vi di chuyển ấn tượng lên đến 431km.', 'ElectricCar', 1, 75.30, NOW(),
         1, 1, 'https://example.com/images/vf7-main.jpg',
         '["https://example.com/images/vf7-1.jpg", "https://example.com/images/vf7-2.jpg"]',
         '["Hiển thị kính lái HUD", "Camera 360", "Hệ thống 8 loa", "ADAS nâng cao"]');

    -- Mẫu 6: VinFast VF 8 Plus
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0006-vf8', 'VinFast VF 8 Plus', 2023, '43A-306.66', 5, 18000,
         '{"hourly": 170000, "daily": 1500000, "monthly": 30000000}',
         'Available', 'SUV điện 5 chỗ hạng D sang trọng, phù hợp cho các chuyến công tác hoặc du lịch gia đình. Phạm vi di chuyển lên tới 447km.', 'ElectricCar', 1, 87.70, NOW(),
         1, 1, 'https://example.com/images/vf8-plus-main.jpg',
         '["https://example.com/images/vf8-plus-1.jpg", "https://example.com/images/vf8-plus-2.jpg"]',
         '["GPS", "Camera 360", "Cửa sổ trời toàn cảnh", "Ghế da chỉnh điện", "ADAS"]');

    -- Mẫu 7: VinFast VF 9
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0007-vf9', 'VinFast VF 9', 2023, '43A-307.77', 6, 9500,
         '{"hourly": 250000, "daily": 2200000, "monthly": 45000000}',
         'Available', 'Mẫu E-SUV full-size hạng sang với 6 chỗ ngồi và hàng ghế cơ trưởng. Lựa chọn đẳng cấp cho những chuyến đi đặc biệt. Phạm vi 423km.', 'ElectricCar', 1, 92.00, NOW(),
         1, 1, 'https://example.com/images/vf9-6s-main.jpg',
         '["https://example.com/images/vf9-6s-1.jpg", "https://example.com/images/vf9-6s-2.jpg"]',
         '["Hàng ghế cơ trưởng", "Màn hình giải trí cho hàng ghế sau", "Cửa sổ trời toàn cảnh", "ADAS nâng cao", "Hệ thống treo khí nén"]');


    -- =============================================
    -- SECTION 3: DỮ LIỆU XE MÁY ĐIỆN VINFAST
    -- =============================================

    -- Mẫu 8: VinFast Evo 200 Lite
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0008-evoL', 'VinFast Evo 200 Lite', 2023, '43-AB-001.11', 2, 3500,
         '{"hourly": 18000, "daily": 140000, "monthly": 2200000}',
         'Available', 'Phiên bản giới hạn tốc độ, phù hợp cho học sinh, sinh viên và người không cần bằng lái. Di chuyển tới 205km mỗi lần sạc.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/evo200lite-main.jpg',
         '["https://example.com/images/evo200lite-1.jpg"]',
         '["Đèn LED", "Định vị xe (GPS)", "Chìa khóa thông minh"]');

    -- Mẫu 9: VinFast Evo 200
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0009-evo', 'VinFast Evo 200', 2023, '43-AC-002.22', 2, 4200,
         '{"hourly": 20000, "daily": 150000, "monthly": 2500000}',
         'Available', 'Mẫu xe phổ thông với quãng đường di chuyển vượt trội, thiết kế thời trang và vận hành bền bỉ. Quãng đường ~203km.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/evo200-main.jpg',
         '["https://example.com/images/evo200-1.jpg"]',
         '["Đèn Full LED", "Cốp rộng 22L", "Màn hình LCD"]');

    -- Mẫu 10: VinFast Feliz S
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0010-feliz', 'VinFast Feliz S', 2022, '43-AD-003.33', 2, 6800,
         '{"hourly": 20000, "daily": 150000, "monthly": 2500000}',
         'Rented', 'Xe máy điện nhỏ gọn, cốp rộng, phù hợp để dạo quanh thành phố Đà Nẵng. Quãng đường di chuyển ~198km.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/feliz-s-main.jpg',
         '["https://example.com/images/feliz-s-1.jpg"]',
         '["Đèn LED", "Cốp rộng 25L", "Chìa khóa thông minh"]');

    -- Mẫu 11: VinFast Klara S (2022)
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0011-klara', 'VinFast Klara S (2022)', 2022, '43-AE-004.44', 2, 7100,
         '{"hourly": 22000, "daily": 160000, "monthly": 2700000}',
         'Available', 'Thiết kế Ý sang trọng, vận hành thông minh và thân thiện với môi trường. Quãng đường di chuyển lên tới 194km.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/klara-s-main.jpg',
         '["https://example.com/images/klara-s-1.jpg"]',
         '["Thiết kế Ý", "Smart key", "Đèn full LED", "Cốp 23L"]');

    -- Mẫu 12: VinFast Vento S
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0012-vento', 'VinFast Vento S', 2022, '43-AF-005.55', 2, 5300,
         '{"hourly": 25000, "daily": 180000, "monthly": 3000000}',
         'Available', 'Xe tay ga cao cấp với công nghệ PAAK (Phone As A Key) hiện đại, thiết kế lịch lãm, mạnh mẽ. Quãng đường ~160km.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/vento-s-main.jpg',
         '["https://example.com/images/vento-s-1.jpg"]',
         '["Công nghệ PAAK", "Smart key", "Cốp 25L", "Phanh ABS"]');

    -- Mẫu 13: VinFast Theon S
    INSERT INTO `Vehicle`
    (`VehicleId`, `VehicleModel`, `YearManufactured`, `LicensePlate`, `Seats`, `Odometer`, `RentalPrices`, `Status`, `Description`, `VehicleType`, `RequiresLicense`, `BatteryCapacity`, `CreatedDate`, `CategoryId`, `TransmissionTypeId`, `MainImageUrl`, `ImageUrls`, `Features`)
    VALUES
        ('a1b2c3d4-vin-0013-theon', 'VinFast Theon S', 2022, '43-AG-006.66', 2, 4900,
         '{"hourly": 28000, "daily": 200000, "monthly": 3500000}',
         'Maintenance', 'Mẫu xe máy điện cao cấp nhất, hiệu suất vượt trội, công nghệ thông minh PAAK và thiết kế đậm chất thể thao. Quãng đường ~150km.', 'ElectricMotorcycle', 0, 3.50, NOW(),
         2, 1, 'https://example.com/images/theon-s-main.jpg',
         '["https://example.com/images/theon-s-1.jpg"]',
         '["Công nghệ PAAK", "Dẫn động bằng dây xích", "Phanh ABS 2 kênh", "Cốp 24L"]');