-- Add OrderCode column to Payment table
ALTER TABLE Payment ADD COLUMN OrderCode VARCHAR(100) NULL AFTER UserId;

-- Add index for better query performance
CREATE INDEX idx_payment_ordercode ON Payment(OrderCode);
