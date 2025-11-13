-- Add transfer proof image field to RefundRequest table
ALTER TABLE RefundRequest ADD COLUMN TransferProofImagePath VARCHAR(500) NULL;

-- Add index for better query performance
CREATE INDEX idx_refundrequest_status ON RefundRequest(Status);
