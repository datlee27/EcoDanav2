-- Sample Contracts for Testing
-- Make sure you have existing Users and Bookings in your database before running this

-- Insert sample contracts
-- Replace the UserId and BookingId with actual IDs from your database

-- Contract 1: Draft status
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted)
SELECT 
    UUID() as ContractId,
    'CT-20251012140000-ABC123' as ContractCode,
    u.UserId,
    b.BookingId,
    NOW() as CreatedDate,
    'Draft' as Status,
    0 as TermsAccepted
FROM Users u
CROSS JOIN Booking b
WHERE u.Email LIKE '%@%'
AND b.Status = 'Pending'
LIMIT 1;

-- Contract 2: Signed status
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, Status, TermsAccepted, SignatureData, SignatureMethod)
SELECT 
    UUID() as ContractId,
    'CT-20251012140100-DEF456' as ContractCode,
    u.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 2 DAY) as CreatedDate,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as SignedDate,
    'Signed' as Status,
    1 as TermsAccepted,
    'digital-signature-data' as SignatureData,
    'digital' as SignatureMethod
FROM Users u
CROSS JOIN Booking b
WHERE u.Email LIKE '%@%'
AND b.Status = 'Approved'
LIMIT 1;

-- Contract 3: Completed status
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, CompletedDate, Status, TermsAccepted, SignatureData, SignatureMethod, Notes)
SELECT 
    UUID() as ContractId,
    'CT-20251012140200-GHI789' as ContractCode,
    u.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 5 DAY) as CreatedDate,
    DATE_SUB(NOW(), INTERVAL 4 DAY) as SignedDate,
    DATE_SUB(NOW(), INTERVAL 1 DAY) as CompletedDate,
    'Completed' as Status,
    1 as TermsAccepted,
    'digital-signature-data' as SignatureData,
    'digital' as SignatureMethod,
    'Contract completed successfully' as Notes
FROM Users u
CROSS JOIN Booking b
WHERE u.Email LIKE '%@%'
AND b.Status = 'Completed'
LIMIT 1;

-- Contract 4: Cancelled status
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted, CancellationReason)
SELECT 
    UUID() as ContractId,
    'CT-20251012140300-JKL012' as ContractCode,
    u.UserId,
    b.BookingId,
    DATE_SUB(NOW(), INTERVAL 3 DAY) as CreatedDate,
    'Cancelled' as Status,
    0 as TermsAccepted,
    'Customer requested cancellation' as CancellationReason
FROM Users u
CROSS JOIN Booking b
WHERE u.Email LIKE '%@%'
AND b.Status = 'Cancelled'
LIMIT 1;

-- Verify the inserted contracts
SELECT 
    c.ContractCode,
    c.Status,
    CONCAT(u.FirstName, ' ', u.LastName) as CustomerName,
    u.Email,
    v.VehicleModel,
    v.LicensePlate,
    c.CreatedDate,
    c.SignedDate,
    c.CompletedDate
FROM Contract c
JOIN Users u ON c.UserId = u.UserId
JOIN Booking b ON c.BookingId = b.BookingId
JOIN Vehicle v ON b.VehicleId = v.VehicleId
ORDER BY c.CreatedDate DESC;
