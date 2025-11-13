-- Add Transferred status to RefundRequest Status ENUM
ALTER TABLE RefundRequest MODIFY COLUMN Status ENUM('Pending', 'Approved', 'Rejected', 'Transferred', 'Completed', 'Refunded') NOT NULL;
