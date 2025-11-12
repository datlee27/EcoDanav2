-- Make BankAccountId nullable in RefundRequest table
-- Allow RefundRequest to be created without a bank account (admin can add it later)

ALTER TABLE RefundRequest 
MODIFY COLUMN BankAccountId VARCHAR(36) NULL;
