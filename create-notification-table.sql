-- Create Notification table
CREATE TABLE IF NOT EXISTS Notification (
    NotificationId VARCHAR(36) PRIMARY KEY,
    UserId VARCHAR(36) NOT NULL,
    Message TEXT NOT NULL,
    CreatedDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IsRead BOOLEAN NOT NULL DEFAULT FALSE,
    RelatedId VARCHAR(36),
    NotificationType VARCHAR(50),
    FOREIGN KEY (UserId) REFERENCES User(UserId) ON DELETE CASCADE,
    INDEX idx_user_id (UserId),
    INDEX idx_is_read (IsRead),
    INDEX idx_created_date (CreatedDate DESC),
    INDEX idx_related_id (RelatedId),
    INDEX idx_notification_type (NotificationType)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- If table already exists, add new columns
ALTER TABLE Notification 
ADD COLUMN IF NOT EXISTS RelatedId VARCHAR(36),
ADD COLUMN IF NOT EXISTS NotificationType VARCHAR(50),
ADD INDEX IF NOT EXISTS idx_related_id (RelatedId),
ADD INDEX IF NOT EXISTS idx_notification_type (NotificationType);
