-- Tạo sample contracts từ bookings có sẵn
-- Chạy script này trong MySQL

USE ecodanangv2;

-- Xem bookings hiện có
SELECT b.BookingId, b.UserId, b.Status, u.Email, u.FirstName, u.LastName
FROM Booking b
JOIN Users u ON b.UserId = u.UserId
LIMIT 5;

-- Tạo contracts từ bookings có sẵn
-- Contract 1: Draft
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted)
SELECT 
    CONCAT('contract-', UUID()) as ContractId,
    CONCAT('CT-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', UPPER(SUBSTRING(UUID(), 1, 6))) as ContractCode,
    b.UserId,
    b.BookingId,
    NOW() as CreatedDate,
    'Draft' as Status,
    0 as TermsAccepted
FROM Booking b
WHERE b.Status = 'Pending'
LIMIT 1;

-- Contract 2: Signed
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, Status, TermsAccepted, SignatureData, SignatureMethod)
SELECT 
    CONCAT('contract-', UUID()) as ContractId,
    CONCAT('CT-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', UPPER(SUBSTRING(UUID(), 1, 6))) as ContractCode,
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 2 DAY) as CreatedDate,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as SignedDate,
    'Signed' as Status,
    1 as TermsAccepted,
    'digital-signature-data' as SignatureData,
    'digital' as SignatureMethod
FROM Booking b
WHERE b.Status IN ('Approved', 'Confirmed')
LIMIT 1;

-- Contract 3: Completed
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, CompletedDate, Status, TermsAccepted, SignatureData, SignatureMethod, Notes)
SELECT 
    CONCAT('contract-', UUID()) as ContractId,
    CONCAT('CT-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', UPPER(SUBSTRING(UUID(), 1, 6))) as ContractCode,
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 5 DAY) as CreatedDate,
    DATE_SUB(NOW(), INTERVAL 4 DAY) as SignedDate,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as CompletedDate,
    'Completed' as Status,
    1 as TermsAccepted,
    'digital-signature-data' as SignatureData,
    'digital' as SignatureMethod,
    'Contract completed successfully' as Notes
FROM Booking b
WHERE b.Status = 'Completed'
LIMIT 1;

-- Contract 4: Cancelled
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted, CancellationReason)
SELECT 
    CONCAT('contract-', UUID()) as ContractId,
    CONCAT('CT-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', UPPER(SUBSTRING(UUID(), 1, 6))) as ContractCode,
    b.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 3 DAY) as CreatedDate,
    'Cancelled' as Status,
    0 as TermsAccepted,
    'Customer requested cancellation' as CancellationReason
FROM Booking b
WHERE b.Status = 'Cancelled'
LIMIT 1;

-- Nếu không có bookings với status phù hợp, tạo từ bất kỳ booking nào
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted)
SELECT 
    CONCAT('contract-', UUID()) as ContractId,
    CONCAT('CT-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '-', UPPER(SUBSTRING(UUID(), 1, 6))) as ContractCode,
    b.UserId,
    b.BookingId,
    NOW() as CreatedDate,
    'Draft' as Status,
    0 as TermsAccepted
FROM Booking b
LIMIT 3;

-- Xem kết quả
SELECT 
    c.ContractCode,
    c.Status,
    CONCAT(u.FirstName, ' ', u.LastName) as CustomerName,
    u.Email,
    b.BookingCode,
    c.CreatedDate,
    c.SignedDate
FROM Contract c
JOIN Users u ON c.UserId = u.UserId
JOIN Booking b ON c.BookingId = b.BookingId
ORDER BY c.CreatedDate DESC;
