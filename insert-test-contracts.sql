USE ecodanangv2;

-- Tạo contracts trực tiếp với UUID
DELETE FROM Contract WHERE ContractCode LIKE 'CT-TEST%';

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted) VALUES
(UUID(), 'CT-TEST-001', 
 (SELECT UserId FROM Users LIMIT 1), 
 (SELECT BookingId FROM Booking LIMIT 1), 
 NOW(), 'Draft', 0);

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, Status, TermsAccepted, SignatureData, SignatureMethod) VALUES
(UUID(), 'CT-TEST-002', 
 (SELECT UserId FROM Users LIMIT 1 OFFSET 1), 
 (SELECT BookingId FROM Booking LIMIT 1 OFFSET 1), 
 DATE_SUB(NOW(), INTERVAL 2 DAY), 
 DATE_SUB(NOW(), INTERVAL 1 DAY), 
 'Signed', 1, 'digital-signature', 'digital');

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, SignedDate, CompletedDate, Status, TermsAccepted, SignatureData, SignatureMethod, Notes) VALUES
(UUID(), 'CT-TEST-003', 
 (SELECT UserId FROM Users LIMIT 1), 
 (SELECT BookingId FROM Booking LIMIT 1 OFFSET 2), 
 DATE_SUB(NOW(), INTERVAL 5 DAY), 
 DATE_SUB(NOW(), INTERVAL 4 DAY), 
 DATE_SUB(NOW(), INTERVAL 1 DAY), 
 'Completed', 1, 'digital-signature', 'digital', 'Completed successfully');

INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, CreatedDate, Status, TermsAccepted, CancellationReason) VALUES
(UUID(), 'CT-TEST-004', 
 (SELECT UserId FROM Users LIMIT 1 OFFSET 1), 
 (SELECT BookingId FROM Booking LIMIT 1 OFFSET 3), 
 DATE_SUB(NOW(), INTERVAL 3 DAY), 
 'Cancelled', 0, 'Customer cancelled');

-- Xem kết quả
SELECT 
    c.ContractCode,
    c.Status,
    u.Email,
    CONCAT(u.FirstName, ' ', u.LastName) as CustomerName,
    c.CreatedDate
FROM Contract c
JOIN Users u ON c.UserId = u.UserId
ORDER BY c.CreatedDate DESC;
