-- Payment Flow V2 Migration
-- Three-party system: Customer → Staff → Owner

-- ============================================
-- 1. Update Booking table
-- ============================================

-- Add approval tracking
ALTER TABLE Booking ADD COLUMN ApprovedBy CHAR(36) NULL COMMENT 'Owner who approved the booking';
ALTER TABLE Booking ADD COLUMN ApprovedDate DATETIME NULL COMMENT 'When booking was approved';

-- Add rejection tracking
ALTER TABLE Booking ADD COLUMN RejectedBy CHAR(36) NULL COMMENT 'Owner who rejected the booking';
ALTER TABLE Booking ADD COLUMN RejectedDate DATETIME NULL COMMENT 'When booking was rejected';

-- Add pickup tracking
ALTER TABLE Booking ADD COLUMN PickupConfirmedBy CHAR(36) NULL COMMENT 'Staff/Owner who confirmed pickup';
ALTER TABLE Booking ADD COLUMN ActualPickupDate DATETIME NULL COMMENT 'Actual pickup date/time';

-- Add return tracking (dual confirmation)
ALTER TABLE Booking ADD COLUMN ReturnConfirmedByCustomer BOOLEAN DEFAULT FALSE COMMENT 'Customer confirmed return';
ALTER TABLE Booking ADD COLUMN CustomerReturnDate DATETIME NULL COMMENT 'When customer confirmed return';
ALTER TABLE Booking ADD COLUMN ReturnConfirmedByOwner BOOLEAN DEFAULT FALSE COMMENT 'Owner confirmed receiving vehicle';
ALTER TABLE Booking ADD COLUMN OwnerConfirmDate DATETIME NULL COMMENT 'When owner confirmed return';
ALTER TABLE Booking ADD COLUMN ActualReturnDate DATETIME NULL COMMENT 'Actual return date/time (when both confirmed)';

-- Add foreign key constraints
ALTER TABLE Booking ADD CONSTRAINT fk_approved_by FOREIGN KEY (ApprovedBy) REFERENCES Users(UserId) ON DELETE SET NULL;
ALTER TABLE Booking ADD CONSTRAINT fk_rejected_by FOREIGN KEY (RejectedBy) REFERENCES Users(UserId) ON DELETE SET NULL;
ALTER TABLE Booking ADD CONSTRAINT fk_pickup_confirmed_by FOREIGN KEY (PickupConfirmedBy) REFERENCES Users(UserId) ON DELETE SET NULL;

-- Add indexes
CREATE INDEX idx_booking_approved_by ON Booking(ApprovedBy);
CREATE INDEX idx_booking_rejected_by ON Booking(RejectedBy);
CREATE INDEX idx_booking_return_status ON Booking(ReturnConfirmedByCustomer, ReturnConfirmedByOwner);

-- ============================================
-- 2. Update Payment table
-- ============================================

-- Add money holding tracking (Staff holds money temporarily)
ALTER TABLE payment ADD COLUMN HeldBy CHAR(36) NULL COMMENT 'Staff who is holding the money';
ALTER TABLE payment ADD COLUMN HeldDate DATETIME NULL COMMENT 'When money was received and held';

-- Add transfer to owner tracking
ALTER TABLE payment ADD COLUMN TransferredToOwner BOOLEAN DEFAULT FALSE COMMENT 'Money transferred to owner';
ALTER TABLE payment ADD COLUMN TransferDate DATETIME NULL COMMENT 'When money was transferred to owner';
ALTER TABLE payment ADD COLUMN TransferredBy CHAR(36) NULL COMMENT 'Staff who transferred money';

-- Add refund tracking
ALTER TABLE payment ADD COLUMN RefundDate DATETIME NULL COMMENT 'When refund was processed';
ALTER TABLE payment ADD COLUMN RefundedBy CHAR(36) NULL COMMENT 'Staff who processed refund';
ALTER TABLE payment ADD COLUMN RefundAmount DECIMAL(10,2) NULL COMMENT 'Refund amount';

-- Add foreign key constraints
ALTER TABLE payment ADD CONSTRAINT fk_held_by FOREIGN KEY (HeldBy) REFERENCES Users(UserId) ON DELETE SET NULL;
ALTER TABLE payment ADD CONSTRAINT fk_transferred_by FOREIGN KEY (TransferredBy) REFERENCES Users(UserId) ON DELETE SET NULL;
ALTER TABLE payment ADD CONSTRAINT fk_refunded_by FOREIGN KEY (RefundedBy) REFERENCES Users(UserId) ON DELETE SET NULL;

-- Add indexes
CREATE INDEX idx_payment_held_by ON payment(HeldBy);
CREATE INDEX idx_payment_transfer_status ON payment(TransferredToOwner);
CREATE INDEX idx_payment_refund_date ON payment(RefundDate);

-- ============================================
-- 3. Update existing data (if any)
-- ============================================

-- Set default values for existing bookings
UPDATE Booking 
SET ReturnConfirmedByCustomer = FALSE,
    ReturnConfirmedByOwner = FALSE
WHERE ReturnConfirmedByCustomer IS NULL;

UPDATE payment 
SET TransferredToOwner = FALSE
WHERE TransferredToOwner IS NULL;

-- ============================================
-- 4. Add new booking status values (if using ENUM)
-- ============================================

-- Note: If Booking.Status is ENUM, you may need to recreate it
-- This is a reference for the new status values:
-- 'PendingPayment'      - Waiting for customer to pay
-- 'PendingOwnerApproval' - Payment received, waiting for owner approval
-- 'Approved'            - Owner approved, ready to pickup
-- 'Rejected'            - Owner rejected, need refund
-- 'Ongoing'             - Currently renting
-- 'Completed'           - Trip completed
-- 'Cancelled'           - Cancelled by customer

-- ============================================
-- 5. Verification queries
-- ============================================

-- Check Booking table structure
DESCRIBE Booking;

-- Check Payment table structure
DESCRIBE payment;

-- Check foreign keys
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'ecodana'
    AND TABLE_NAME IN ('Booking', 'payment')
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Check indexes
SHOW INDEX FROM Booking;
SHOW INDEX FROM payment;

-- ============================================
-- 6. Sample data for testing (optional)
-- ============================================

-- You can add test data here if needed

-- ============================================
-- Rollback script (in case of issues)
-- ============================================

/*
-- To rollback, run these commands:

-- Drop foreign keys
ALTER TABLE Booking DROP FOREIGN KEY fk_approved_by;
ALTER TABLE Booking DROP FOREIGN KEY fk_rejected_by;
ALTER TABLE Booking DROP FOREIGN KEY fk_pickup_confirmed_by;
ALTER TABLE payment DROP FOREIGN KEY fk_held_by;
ALTER TABLE payment DROP FOREIGN KEY fk_transferred_by;
ALTER TABLE payment DROP FOREIGN KEY fk_refunded_by;

-- Drop indexes
DROP INDEX idx_booking_approved_by ON Booking;
DROP INDEX idx_booking_rejected_by ON Booking;
DROP INDEX idx_booking_return_status ON Booking;
DROP INDEX idx_payment_held_by ON payment;
DROP INDEX idx_payment_transfer_status ON payment;
DROP INDEX idx_payment_refund_date ON payment;

-- Drop columns from Booking
ALTER TABLE Booking DROP COLUMN ApprovedBy;
ALTER TABLE Booking DROP COLUMN ApprovedDate;
ALTER TABLE Booking DROP COLUMN RejectedBy;
ALTER TABLE Booking DROP COLUMN RejectedDate;
ALTER TABLE Booking DROP COLUMN PickupConfirmedBy;
ALTER TABLE Booking DROP COLUMN ActualPickupDate;
ALTER TABLE Booking DROP COLUMN ReturnConfirmedByCustomer;
ALTER TABLE Booking DROP COLUMN CustomerReturnDate;
ALTER TABLE Booking DROP COLUMN ReturnConfirmedByOwner;
ALTER TABLE Booking DROP COLUMN OwnerConfirmDate;
ALTER TABLE Booking DROP COLUMN ActualReturnDate;

-- Drop columns from payment
ALTER TABLE payment DROP COLUMN HeldBy;
ALTER TABLE payment DROP COLUMN HeldDate;
ALTER TABLE payment DROP COLUMN TransferredToOwner;
ALTER TABLE payment DROP COLUMN TransferDate;
ALTER TABLE payment DROP COLUMN TransferredBy;
ALTER TABLE payment DROP COLUMN RefundDate;
ALTER TABLE payment DROP COLUMN RefundedBy;
ALTER TABLE payment DROP COLUMN RefundAmount;
*/
