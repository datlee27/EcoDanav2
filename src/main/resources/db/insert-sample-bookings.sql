-- =============================================
-- INSERT SAMPLE BOOKINGS WITH REAL DATA
-- Run this after you have users and vehicles in database
-- =============================================

-- First, let's check what users and vehicles exist
-- SELECT UserId, Username, Email FROM Users LIMIT 5;
-- SELECT VehicleId, VehicleModel, LicensePlate FROM Vehicle LIMIT 5;

-- Sample Bookings (Update the UUIDs with your actual User and Vehicle IDs)
-- Replace 'USER-ID-HERE' with actual user IDs from your Users table
-- Replace 'VEHICLE-ID-HERE' with actual vehicle IDs from your Vehicle table

-- Example: If you have a user with ID 'abc-123' and vehicle with ID 'xyz-456'
INSERT INTO Booking (
    BookingId, 
    UserId, 
    VehicleId, 
    HandledBy, 
    PickupDateTime, 
    ReturnDateTime, 
    TotalAmount, 
    Status, 
    DiscountId, 
    CreatedDate, 
    CancelReason, 
    BookingCode, 
    ExpectedPaymentMethod, 
    RentalType, 
    TermsAgreed, 
    TermsAgreedAt, 
    TermsVersion
) VALUES
-- Pending Booking
(
    UUID(), 
    (SELECT UserId FROM Users WHERE Status = 'Active' LIMIT 1), 
    (SELECT VehicleId FROM Vehicle WHERE Status = 'Available' LIMIT 1), 
    NULL, 
    DATE_ADD(NOW(), INTERVAL 5 DAY), 
    DATE_ADD(NOW(), INTERVAL 7 DAY), 
    1500000.00, 
    'Pending', 
    NULL, 
    NOW(), 
    NULL, 
    CONCAT('BK', UNIX_TIMESTAMP()), 
    'Cash', 
    'daily', 
    1, 
    NOW(), 
    'v1.0'
),
-- Approved Booking
(
    UUID(), 
    (SELECT UserId FROM Users WHERE Status = 'Active' LIMIT 1 OFFSET 1), 
    (SELECT VehicleId FROM Vehicle WHERE Status = 'Available' LIMIT 1 OFFSET 1), 
    (SELECT UserId FROM Users WHERE RoleId = (SELECT RoleId FROM Roles WHERE RoleName = 'ADMIN' LIMIT 1) LIMIT 1), 
    DATE_ADD(NOW(), INTERVAL 3 DAY), 
    DATE_ADD(NOW(), INTERVAL 5 DAY), 
    1200000.00, 
    'Approved', 
    NULL, 
    DATE_SUB(NOW(), INTERVAL 1 DAY), 
    NULL, 
    CONCAT('BK', UNIX_TIMESTAMP() + 1), 
    'VNPay', 
    'daily', 
    1, 
    DATE_SUB(NOW(), INTERVAL 1 DAY), 
    'v1.0'
),
-- Ongoing Booking
(
    UUID(), 
    (SELECT UserId FROM Users WHERE Status = 'Active' LIMIT 1 OFFSET 2), 
    (SELECT VehicleId FROM Vehicle WHERE Status = 'Rented' LIMIT 1), 
    (SELECT UserId FROM Users WHERE RoleId = (SELECT RoleId FROM Roles WHERE RoleName = 'ADMIN' LIMIT 1) LIMIT 1), 
    DATE_SUB(NOW(), INTERVAL 1 DAY), 
    DATE_ADD(NOW(), INTERVAL 2 DAY), 
    1800000.00, 
    'Ongoing', 
    NULL, 
    DATE_SUB(NOW(), INTERVAL 2 DAY), 
    NULL, 
    CONCAT('BK', UNIX_TIMESTAMP() + 2), 
    'Cash', 
    'daily', 
    1, 
    DATE_SUB(NOW(), INTERVAL 2 DAY), 
    'v1.0'
),
-- Completed Booking
(
    UUID(), 
    (SELECT UserId FROM Users WHERE Status = 'Active' LIMIT 1 OFFSET 3), 
    (SELECT VehicleId FROM Vehicle LIMIT 1 OFFSET 3), 
    (SELECT UserId FROM Users WHERE RoleId = (SELECT RoleId FROM Roles WHERE RoleName = 'ADMIN' LIMIT 1) LIMIT 1), 
    DATE_SUB(NOW(), INTERVAL 10 DAY), 
    DATE_SUB(NOW(), INTERVAL 8 DAY), 
    1400000.00, 
    'Completed', 
    NULL, 
    DATE_SUB(NOW(), INTERVAL 12 DAY), 
    NULL, 
    CONCAT('BK', UNIX_TIMESTAMP() + 3), 
    'BankTransfer', 
    'daily', 
    1, 
    DATE_SUB(NOW(), INTERVAL 12 DAY), 
    'v1.0'
),
-- Cancelled Booking
(
    UUID(), 
    (SELECT UserId FROM Users WHERE Status = 'Active' LIMIT 1 OFFSET 4), 
    (SELECT VehicleId FROM Vehicle LIMIT 1 OFFSET 4), 
    (SELECT UserId FROM Users WHERE RoleId = (SELECT RoleId FROM Roles WHERE RoleName = 'ADMIN' LIMIT 1) LIMIT 1), 
    DATE_ADD(NOW(), INTERVAL 15 DAY), 
    DATE_ADD(NOW(), INTERVAL 17 DAY), 
    1300000.00, 
    'Cancelled', 
    NULL, 
    DATE_SUB(NOW(), INTERVAL 3 DAY), 
    'Customer requested cancellation', 
    CONCAT('BK', UNIX_TIMESTAMP() + 4), 
    'Cash', 
    'daily', 
    1, 
    DATE_SUB(NOW(), INTERVAL 3 DAY), 
    'v1.0'
);

-- Verify the inserted data
SELECT 
    b.BookingCode,
    u.Username as CustomerName,
    u.Email as CustomerEmail,
    v.VehicleModel,
    v.LicensePlate,
    b.PickupDateTime,
    b.ReturnDateTime,
    b.Status,
    b.TotalAmount
FROM Booking b
LEFT JOIN Users u ON b.UserId = u.UserId
LEFT JOIN Vehicle v ON b.VehicleId = v.VehicleId
ORDER BY b.CreatedDate DESC;
