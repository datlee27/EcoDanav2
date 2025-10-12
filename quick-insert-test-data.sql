USE ecodanangv2;

-- ===================================
-- BƯỚC 1: Tạo Vehicles nếu chưa có
-- ===================================
INSERT INTO Vehicle (VehicleId, VehicleModel, LicensePlate, Status, RentalPrices, VehicleType, Seats, YearManufactured, Odometer, CreatedDate)
SELECT 
    UUID(),
    'Tesla Model 3',
    '30A-12345',
    'Available',
    '{"daily": 800000, "weekly": 5000000, "monthly": 18000000}',
    'ElectricCar',
    5,
    2023,
    15000,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM Vehicle WHERE LicensePlate = '30A-12345');

INSERT INTO Vehicle (VehicleId, VehicleModel, LicensePlate, Status, RentalPrices, VehicleType, Seats, YearManufactured, Odometer, CreatedDate)
SELECT 
    UUID(),
    'VinFast VF8',
    '30B-67890',
    'Available',
    '{"daily": 750000, "weekly": 4500000, "monthly": 16000000}',
    'ElectricCar',
    5,
    2023,
    12000,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM Vehicle WHERE LicensePlate = '30B-67890');

INSERT INTO Vehicle (VehicleId, VehicleModel, LicensePlate, Status, RentalPrices, VehicleType, Seats, YearManufactured, Odometer, CreatedDate)
SELECT 
    UUID(),
    'VinFast VF5',
    '30C-11111',
    'Available',
    '{"daily": 600000, "weekly": 3600000, "monthly": 12000000}',
    'ElectricCar',
    5,
    2023,
    8000,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM Vehicle WHERE LicensePlate = '30C-11111');

INSERT INTO Vehicle (VehicleId, VehicleModel, LicensePlate, Status, RentalPrices, VehicleType, Seats, YearManufactured, Odometer, CreatedDate)
SELECT 
    UUID(),
    'Yadea Electric Scooter',
    '30D-22222',
    'Available',
    '{"daily": 150000, "weekly": 900000, "monthly": 3000000}',
    'ElectricMotorcycle',
    2,
    2023,
    5000,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM Vehicle WHERE LicensePlate = '30D-22222');

-- ===================================
-- BƯỚC 2: Tạo Bookings
-- ===================================
INSERT INTO Booking (
    BookingId, 
    UserId, 
    VehicleId, 
    PickupDateTime, 
    ReturnDateTime, 
    TotalAmount, 
    Status, 
    CreatedDate, 
    BookingCode, 
    ExpectedPaymentMethod, 
    RentalType, 
    TermsAgreed, 
    TermsAgreedAt, 
    TermsVersion
)
SELECT 
    UUID(),
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '30A-12345' LIMIT 1),
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    1000000.00,
    'Pending',
    NOW(),
    CONCAT('BK', UNIX_TIMESTAMP(), '01'),
    'Cash',
    'daily',
    1,
    NOW(),
    'v1.0'
WHERE NOT EXISTS (SELECT 1 FROM Booking WHERE BookingCode = CONCAT('BK', UNIX_TIMESTAMP(), '01'));

INSERT INTO Booking (
    BookingId, 
    UserId, 
    VehicleId, 
    HandledBy,
    PickupDateTime, 
    ReturnDateTime, 
    TotalAmount, 
    Status, 
    CreatedDate, 
    BookingCode, 
    ExpectedPaymentMethod, 
    RentalType, 
    TermsAgreed, 
    TermsAgreedAt, 
    TermsVersion
)
SELECT 
    UUID(),
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '30B-67890' LIMIT 1),
    (SELECT UserId FROM Users WHERE Email = 'admin@ecodana.com' LIMIT 1),
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    1100000.00,
    'Approved',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    CONCAT('BK', UNIX_TIMESTAMP(), '02'),
    'VNPay',
    'daily',
    1,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'v1.0'
WHERE NOT EXISTS (SELECT 1 FROM Booking WHERE BookingCode = CONCAT('BK', UNIX_TIMESTAMP(), '02'));

INSERT INTO Booking (
    BookingId, 
    UserId, 
    VehicleId, 
    HandledBy,
    PickupDateTime, 
    ReturnDateTime, 
    TotalAmount, 
    Status, 
    CreatedDate, 
    BookingCode, 
    ExpectedPaymentMethod, 
    RentalType, 
    TermsAgreed, 
    TermsAgreedAt, 
    TermsVersion
)
SELECT 
    UUID(),
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '30C-11111' LIMIT 1),
    (SELECT UserId FROM Users WHERE Email = 'admin@ecodana.com' LIMIT 1),
    DATE_SUB(NOW(), INTERVAL 10 DAY),
    DATE_SUB(NOW(), INTERVAL 8 DAY),
    1200000.00,
    'Completed',
    DATE_SUB(NOW(), INTERVAL 12 DAY),
    CONCAT('BK', UNIX_TIMESTAMP(), '03'),
    'BankTransfer',
    'daily',
    1,
    DATE_SUB(NOW(), INTERVAL 12 DAY),
    'v1.0'
WHERE NOT EXISTS (SELECT 1 FROM Booking WHERE BookingCode = CONCAT('BK', UNIX_TIMESTAMP(), '03'));

INSERT INTO Booking (
    BookingId, 
    UserId, 
    VehicleId, 
    HandledBy,
    PickupDateTime, 
    ReturnDateTime, 
    TotalAmount, 
    Status, 
    CreatedDate, 
    CancelReason,
    BookingCode, 
    ExpectedPaymentMethod, 
    RentalType, 
    TermsAgreed, 
    TermsAgreedAt, 
    TermsVersion
)
SELECT 
    UUID(),
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '30D-22222' LIMIT 1),
    (SELECT UserId FROM Users WHERE Email = 'admin@ecodana.com' LIMIT 1),
    DATE_ADD(NOW(), INTERVAL 15 DAY),
    DATE_ADD(NOW(), INTERVAL 17 DAY),
    960000.00,
    'Cancelled',
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    'Customer requested cancellation',
    CONCAT('BK', UNIX_TIMESTAMP(), '04'),
    'Cash',
    'daily',
    1,
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    'v1.0'
WHERE NOT EXISTS (SELECT 1 FROM Booking WHERE BookingCode = CONCAT('BK', UNIX_TIMESTAMP(), '04'));

-- ===================================
-- BƯỚC 3: Tạo Contracts
-- ===================================
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted)
SELECT 
    UUID(),
    CONCAT('CT', UNIX_TIMESTAMP(), '01'),
    b.UserId,
    b.BookingId,
    NOW(),
    'Draft',
    0
FROM Booking b
WHERE b.Status = 'Pending'
AND NOT EXISTS (SELECT 1 FROM Contract WHERE BookingId = b.BookingId)
LIMIT 1;

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, Status, TermsAccepted, SignatureData, SignatureMethod)
SELECT 
    UUID(),
    CONCAT('CT', UNIX_TIMESTAMP(), '02'),
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'Signed',
    1,
    'digital-signature-data',
    'digital'
FROM Booking b
WHERE b.Status = 'Approved'
AND NOT EXISTS (SELECT 1 FROM Contract WHERE BookingId = b.BookingId)
LIMIT 1;

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, CompletedDate, Status, TermsAccepted, SignatureData, SignatureMethod, Notes)
SELECT 
    UUID(),
    CONCAT('CT', UNIX_TIMESTAMP(), '03'),
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    DATE_SUB(NOW(), INTERVAL 4 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'Completed',
    1,
    'digital-signature-data',
    'digital',
    'Contract completed successfully'
FROM Booking b
WHERE b.Status = 'Completed'
AND NOT EXISTS (SELECT 1 FROM Contract WHERE BookingId = b.BookingId)
LIMIT 1;

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted, CancellationReason)
SELECT 
    UUID(),
    CONCAT('CT', UNIX_TIMESTAMP(), '04'),
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    'Cancelled',
    0,
    'Customer requested cancellation'
FROM Booking b
WHERE b.Status = 'Cancelled'
AND NOT EXISTS (SELECT 1 FROM Contract WHERE BookingId = b.BookingId)
LIMIT 1;

-- ===================================
-- KIỂM TRA KẾT QUẢ
-- ===================================
SELECT 'Vehicles' as TableName, COUNT(*) as TotalRecords FROM Vehicle
UNION ALL
SELECT 'Bookings', COUNT(*) FROM Booking
UNION ALL
SELECT 'Contracts', COUNT(*) FROM Contract;

-- Xem chi tiết contracts
SELECT 
    c.ContractCode,
    c.Status,
    CONCAT(u.FirstName, ' ', u.LastName) as CustomerName,
    u.Email,
    b.BookingCode,
    v.VehicleModel,
    c.CreatedDate,
    c.SignedDate
FROM Contract c
JOIN Users u ON c.UserId = u.UserId
JOIN Booking b ON c.BookingId = b.BookingId
JOIN Vehicle v ON b.VehicleId = v.VehicleId
ORDER BY c.CreatedDate DESC;
