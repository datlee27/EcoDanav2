-- Update Vehicle table to support PendingApproval status
-- Run this SQL in your MySQL database

USE ecodana;

-- Check current status values
SELECT Status, COUNT(*) as count FROM Vehicle GROUP BY Status;

-- Alter the Status column to support new enum value
-- If Status is VARCHAR, no need to alter
-- If Status is ENUM, need to modify

-- Check column type first
SHOW COLUMNS FROM Vehicle LIKE 'Status';

-- If it's ENUM, modify it (uncomment if needed):
-- ALTER TABLE Vehicle MODIFY COLUMN Status ENUM('PendingApproval', 'Available', 'Rented', 'Maintenance', 'Unavailable');

-- If it's VARCHAR, just ensure it's long enough:
ALTER TABLE Vehicle MODIFY COLUMN Status VARCHAR(20);

-- Set all existing vehicles to Available if they don't have a valid status
UPDATE Vehicle 
SET Status = 'Available' 
WHERE Status IS NULL OR Status = '' OR Status NOT IN ('PendingApproval', 'Available', 'Rented', 'Maintenance', 'Unavailable');

SELECT 'Migration completed successfully!' AS Result;
SELECT Status, COUNT(*) as count FROM Vehicle GROUP BY Status;
