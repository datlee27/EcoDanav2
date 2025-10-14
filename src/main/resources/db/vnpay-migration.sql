-- =====================================================
-- VNPay Payment Integration - Database Migration
-- =====================================================
-- Date: 2025-01-13
-- Description: Add PaymentStatus column to Booking table
--              for VNPay payment integration
-- =====================================================

-- Add PaymentStatus column to Booking table
ALTER TABLE Booking 
ADD COLUMN PaymentStatus VARCHAR(20) DEFAULT 'Unpaid'
COMMENT 'Payment status: Unpaid, Paid, Refunded, Partial';

-- Update existing bookings to have default payment status
UPDATE Booking 
SET PaymentStatus = 'Unpaid' 
WHERE PaymentStatus IS NULL;

-- Create index for faster payment status queries
CREATE INDEX idx_booking_payment_status 
ON Booking(PaymentStatus);

-- Create index for faster booking code queries (used in VNPay callbacks)
CREATE INDEX idx_booking_code 
ON Booking(BookingCode);

-- Payment table already exists with correct structure:
-- PaymentId, BookingId, ContractId, Amount, PaymentMethod, 
-- PaymentStatus, PaymentType, TransactionId, PaymentDate, 
-- UserId, Notes, CreateDate

-- Create indexes for Payment table (if not exist)
CREATE INDEX idx_payment_booking 
ON payment(BookingId);

CREATE INDEX idx_payment_transaction 
ON payment(TransactionId);

CREATE INDEX idx_payment_user 
ON payment(UserId);

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check if PaymentStatus column exists in Booking table
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_DEFAULT, 
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Booking' 
  AND COLUMN_NAME = 'PaymentStatus';

-- Check Payment table structure
DESCRIBE payment;

-- Check indexes on Booking table
SHOW INDEX FROM Booking WHERE Key_name LIKE 'idx_%';

-- Check indexes on Payment table
SHOW INDEX FROM payment WHERE Key_name LIKE 'idx_%';

-- =====================================================
-- Sample Queries for Testing
-- =====================================================

-- Get all bookings with payment status
SELECT 
    b.BookingId,
    b.BookingCode,
    b.Status,
    b.PaymentStatus,
    b.TotalAmount,
    b.CreatedDate,
    u.Email as CustomerEmail
FROM Booking b
JOIN User u ON b.UserId = u.UserId
ORDER BY b.CreatedDate DESC
LIMIT 10;

-- Get all payments with booking info
SELECT 
    p.PaymentId,
    p.TransactionId,
    p.Amount,
    p.PaymentMethod,
    p.PaymentStatus,
    p.PaymentType,
    p.PaymentDate,
    b.BookingCode,
    b.Status as BookingStatus,
    u.Email as CustomerEmail
FROM payment p
JOIN Booking b ON p.BookingId = b.BookingId
JOIN User u ON p.UserId = u.UserId
ORDER BY p.CreateDate DESC
LIMIT 10;

-- Get bookings with pending payments
SELECT 
    b.BookingId,
    b.BookingCode,
    b.PaymentStatus,
    b.TotalAmount,
    COUNT(p.PaymentId) as PaymentCount,
    SUM(CASE WHEN p.PaymentStatus = 'Completed' THEN p.Amount ELSE 0 END) as PaidAmount
FROM Booking b
LEFT JOIN payment p ON b.BookingId = p.BookingId
WHERE b.PaymentStatus = 'Unpaid'
GROUP BY b.BookingId, b.BookingCode, b.PaymentStatus, b.TotalAmount
ORDER BY b.CreatedDate DESC;

-- Get payment statistics
SELECT 
    PaymentStatus,
    PaymentMethod,
    COUNT(*) as TransactionCount,
    SUM(Amount) as TotalAmount,
    AVG(Amount) as AverageAmount
FROM payment
GROUP BY PaymentStatus, PaymentMethod
ORDER BY PaymentStatus, TotalAmount DESC;

-- =====================================================
-- Rollback Script (if needed)
-- =====================================================

-- WARNING: Only run this if you need to rollback the changes
-- This will remove the PaymentStatus column and indexes

/*
-- Remove indexes
DROP INDEX IF EXISTS idx_booking_payment_status ON Booking;
DROP INDEX IF EXISTS idx_booking_code ON Booking;
DROP INDEX IF EXISTS idx_payment_booking ON payment;
DROP INDEX IF EXISTS idx_payment_transaction ON payment;
DROP INDEX IF EXISTS idx_payment_user ON payment;

-- Remove PaymentStatus column
ALTER TABLE Booking DROP COLUMN IF EXISTS PaymentStatus;
*/
