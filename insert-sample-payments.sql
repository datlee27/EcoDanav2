-- Insert Sample Payments for EcoDana
-- Make sure you have Users, Vehicles, Bookings, and Contracts in database first

-- Payment 1: Completed Deposit for first booking
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Signed' LIMIT 1),
    500000.00,
    'Credit Card',
    'Completed',
    'Deposit',
    CONCAT('TXN', UNIX_TIMESTAMP(), '001'),
    NOW() - INTERVAL 2 DAY,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Deposit payment for Tesla Model 3 rental',
    NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '001')
);

-- Payment 2: Completed Final Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Completed' LIMIT 1),
    2500000.00,
    'Bank Transfer',
    'Completed',
    'FinalPayment',
    CONCAT('TXN', UNIX_TIMESTAMP(), '002'),
    NOW() - INTERVAL 1 DAY,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Final payment for completed rental',
    NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '002')
);

-- Payment 3: Pending Deposit
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Draft' LIMIT 1),
    300000.00,
    'E-Wallet',
    'Pending',
    'Deposit',
    CONCAT('TXN', UNIX_TIMESTAMP(), '003'),
    NULL,
    (SELECT UserId FROM Users WHERE Email = 'staff@ecodana.com' LIMIT 1),
    'Awaiting deposit payment',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '003')
);

-- Payment 4: Failed Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    NULL,
    450000.00,
    'Credit Card',
    'Failed',
    'Deposit',
    CONCAT('TXN', UNIX_TIMESTAMP(), '004'),
    NOW() - INTERVAL 3 HOUR,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Payment failed - insufficient funds',
    NOW() - INTERVAL 3 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '004')
);

-- Payment 5: Completed Surcharge
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Completed' LIMIT 1),
    150000.00,
    'Cash',
    'Completed',
    'Surcharge',
    CONCAT('TXN', UNIX_TIMESTAMP(), '005'),
    NOW() - INTERVAL 5 HOUR,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Late return surcharge',
    NOW() - INTERVAL 5 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '005')
);

-- Payment 6: Refunded Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Cancelled' LIMIT 1),
    500000.00,
    'Bank Transfer',
    'Refunded',
    'Refund',
    CONCAT('TXN', UNIX_TIMESTAMP(), '006'),
    NOW() - INTERVAL 1 DAY,
    (SELECT UserId FROM Users WHERE Email = 'staff@ecodana.com' LIMIT 1),
    'Refund for cancelled booking',
    NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '006')
);

-- Payment 7: Pending Final Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Signed' LIMIT 1),
    1800000.00,
    'Credit Card',
    'Pending',
    'FinalPayment',
    CONCAT('TXN', UNIX_TIMESTAMP(), '007'),
    NULL,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Awaiting final payment',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '007')
);

-- Payment 8: Completed Deposit with VNPay
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Signed' LIMIT 1),
    400000.00,
    'VNPay',
    'Completed',
    'Deposit',
    CONCAT('TXN', UNIX_TIMESTAMP(), '008'),
    NOW() - INTERVAL 6 HOUR,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'VNPay online payment',
    NOW() - INTERVAL 6 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '008')
);

-- Payment 9: Completed with MoMo
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Signed' LIMIT 1),
    350000.00,
    'MoMo',
    'Completed',
    'Deposit',
    CONCAT('TXN', UNIX_TIMESTAMP(), '009'),
    NOW() - INTERVAL 8 HOUR,
    (SELECT UserId FROM Users WHERE Email = 'staff@ecodana.com' LIMIT 1),
    'MoMo e-wallet payment',
    NOW() - INTERVAL 8 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '009')
);

-- Payment 10: Large Completed Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes, CreatedDate)
SELECT 
    UUID(),
    (SELECT BookingId FROM Booking WHERE BookingCode LIKE 'BK%' LIMIT 1),
    (SELECT ContractId FROM Contract WHERE Status = 'Completed' LIMIT 1),
    5000000.00,
    'Bank Transfer',
    'Completed',
    'FinalPayment',
    CONCAT('TXN', UNIX_TIMESTAMP(), '010'),
    NOW() - INTERVAL 12 HOUR,
    (SELECT UserId FROM Users WHERE Email = 'customer@ecodana.com' LIMIT 1),
    'Full payment for luxury vehicle rental',
    NOW() - INTERVAL 12 HOUR
WHERE NOT EXISTS (
    SELECT 1 FROM Payment WHERE TransactionId = CONCAT('TXN', UNIX_TIMESTAMP(), '010')
);

-- Verify inserted payments
SELECT 
    p.PaymentId,
    p.Amount,
    p.PaymentMethod,
    p.PaymentStatus,
    p.PaymentType,
    p.TransactionId,
    b.BookingCode,
    CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
    p.CreatedDate
FROM Payment p
LEFT JOIN Booking b ON p.BookingId = b.BookingId
LEFT JOIN Users u ON p.UserId = u.UserId
ORDER BY p.CreatedDate DESC
LIMIT 10;

-- Statistics
SELECT 
    PaymentStatus,
    COUNT(*) AS Count,
    SUM(Amount) AS TotalAmount
FROM Payment
GROUP BY PaymentStatus;
