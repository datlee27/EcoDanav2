-- Migration: Remove PaymentStatus column from Booking table
-- Reason: PaymentStatus is now managed through the Payment entity relationship
-- Date: 2025-10-14

USE ecodanangv2;

-- Drop the PaymentStatus column from booking table
-- Note: This will fail if column doesn't exist, which is fine if already removed
ALTER TABLE booking DROP COLUMN PaymentStatus;

-- Verify the change
DESCRIBE booking;
