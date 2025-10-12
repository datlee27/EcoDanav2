-- Insert Sample Discounts for EcoDana

-- Discount 1: Percentage Discount - New Customer
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'New Customer 10%',
    'Welcome discount for new customers',
    'Percentage',
    10.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 30 DAY),
    TRUE,
    'NEWCUST10',
    500000.00,
    200000.00,
    100,
    5,
    'Customer',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'NEWCUST10'
);

-- Discount 2: Fixed Amount Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Flash Sale 100K',
    'Flash sale discount 100,000 VND',
    'FixedAmount',
    100000.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 7 DAY),
    TRUE,
    'FLASH100K',
    1000000.00,
    NULL,
    50,
    12,
    'Promotion',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'FLASH100K'
);

-- Discount 3: Weekend Special
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Weekend Special 15%',
    'Special discount for weekend bookings',
    'Percentage',
    15.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 60 DAY),
    TRUE,
    'WEEKEND15',
    800000.00,
    300000.00,
    200,
    45,
    'Seasonal',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'WEEKEND15'
);

-- Discount 4: Inactive/Expired Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Tet Holiday 20%',
    'Lunar New Year special discount',
    'Percentage',
    20.00,
    DATE_SUB(CURDATE(), INTERVAL 60 DAY),
    DATE_SUB(CURDATE(), INTERVAL 30 DAY),
    FALSE,
    'TET2025',
    1500000.00,
    500000.00,
    150,
    89,
    'Seasonal',
    DATE_SUB(NOW(), INTERVAL 60 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'TET2025'
);

-- Discount 5: VIP Customer Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'VIP Member 25%',
    'Exclusive discount for VIP members',
    'Percentage',
    25.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 90 DAY),
    TRUE,
    'VIP25',
    2000000.00,
    1000000.00,
    NULL,
    23,
    'VIP',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'VIP25'
);

-- Discount 6: Early Bird Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Early Bird 50K',
    'Book 7 days in advance and save 50K',
    'FixedAmount',
    50000.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 45 DAY),
    TRUE,
    'EARLY50K',
    600000.00,
    NULL,
    300,
    67,
    'Promotion',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'EARLY50K'
);

-- Discount 7: Long Term Rental
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Long Term 30%',
    'Discount for rentals over 7 days',
    'Percentage',
    30.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 120 DAY),
    TRUE,
    'LONGTERM30',
    3000000.00,
    1500000.00,
    NULL,
    15,
    'General',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'LONGTERM30'
);

-- Discount 8: Student Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Student Discount 12%',
    'Special discount for students with valid ID',
    'Percentage',
    12.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 180 DAY),
    TRUE,
    'STUDENT12',
    400000.00,
    150000.00,
    500,
    78,
    'Customer',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'STUDENT12'
);

-- Discount 9: Referral Bonus
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Referral Bonus 200K',
    'Bonus for successful referrals',
    'FixedAmount',
    200000.00,
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 365 DAY),
    TRUE,
    'REFER200K',
    1000000.00,
    NULL,
    NULL,
    34,
    'Referral',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'REFER200K'
);

-- Discount 10: Inactive - Used Up
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory, CreatedDate)
SELECT 
    UUID(),
    'Black Friday 40%',
    'Black Friday mega sale',
    'Percentage',
    40.00,
    DATE_SUB(CURDATE(), INTERVAL 30 DAY),
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    FALSE,
    'BLACKFRI40',
    2000000.00,
    800000.00,
    100,
    100,
    'Promotion',
    DATE_SUB(NOW(), INTERVAL 30 DAY)
WHERE NOT EXISTS (
    SELECT 1 FROM Discount WHERE VoucherCode = 'BLACKFRI40'
);

-- Verify inserted discounts
SELECT 
    DiscountId,
    DiscountName,
    DiscountType,
    DiscountValue,
    VoucherCode,
    CONCAT(StartDate, ' - ', EndDate) AS ValidPeriod,
    IsActive,
    CONCAT(UsedCount, '/', COALESCE(UsageLimit, 'Unlimited')) AS `Usage`,
    DiscountCategory
FROM Discount
ORDER BY CreatedDate DESC;

-- Statistics
SELECT 
    DiscountType,
    COUNT(*) AS Count,
    SUM(CASE WHEN IsActive = TRUE THEN 1 ELSE 0 END) AS Active,
    SUM(UsedCount) AS TotalUsed
FROM Discount
GROUP BY DiscountType;
