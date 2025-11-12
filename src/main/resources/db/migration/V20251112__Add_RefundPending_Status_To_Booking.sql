-- Add RefundPending status to Booking table
-- This migration adds the 'RefundPending' status to the Booking Status ENUM
-- RefundPending is used when a customer cancels a booking and the refund is awaiting admin approval

ALTER TABLE Booking 
MODIFY COLUMN Status ENUM(
    'Pending',
    'Approved',
    'AwaitingDeposit',
    'Confirmed',
    'Rejected',
    'Ongoing',
    'Completed',
    'Cancelled',
    'RefundPending'
) NOT NULL DEFAULT 'Pending';
