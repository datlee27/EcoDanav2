-- Migration: Thêm cột NotificationType và RelatedId vào bảng Notification
-- Chạy script này để cập nhật database

USE ecodanav2;

-- Thêm cột NotificationType
ALTER TABLE Notification 
ADD COLUMN NotificationType VARCHAR(50) NULL AFTER Message;

-- Thêm cột RelatedId
ALTER TABLE Notification 
ADD COLUMN RelatedId VARCHAR(36) NULL AFTER NotificationType;

-- Kiểm tra kết quả
DESCRIBE Notification;

-- Cập nhật dữ liệu cũ (nếu có)
UPDATE Notification 
SET NotificationType = 'GENERAL' 
WHERE NotificationType IS NULL;
