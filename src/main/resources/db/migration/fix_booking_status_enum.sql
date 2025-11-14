-- Fix Booking Status ENUM to include all 11 values
-- Run this script to update your existing database

ALTER TABLE `Booking` 
MODIFY COLUMN `Status` ENUM(
    'Pending', 
    'Approved', 
    'Rejected', 
    'Ongoing', 
    'Completed', 
    'Cancelled', 
    'AwaitingDeposit', 
    'Confirmed', 
    'RefundPending', 
    'LatePickup', 
    'NoShowReported'
) NOT NULL DEFAULT 'Pending';
