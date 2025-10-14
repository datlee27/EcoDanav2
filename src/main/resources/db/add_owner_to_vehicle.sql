-- Add OwnerId column to Vehicle table
ALTER TABLE Vehicle 
ADD COLUMN OwnerId VARCHAR(36) NULL AFTER LastUpdatedBy;

-- Add foreign key constraint
ALTER TABLE Vehicle
ADD CONSTRAINT FK_Vehicle_Owner 
FOREIGN KEY (OwnerId) REFERENCES Users(UserId) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- Optional: Set a default owner for existing vehicles
-- UPDATE Vehicle SET OwnerId = (SELECT UserId FROM Users WHERE RoleName = 'OWNER' LIMIT 1) WHERE OwnerId IS NULL;
