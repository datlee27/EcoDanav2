-- Insert test data for EvoDana application

-- Insert Roles
INSERT INTO Roles (RoleId, RoleName, Description) VALUES 
('1', 'Admin', 'System Administrator'),
('2', 'Owner', 'Vehicle Owner'),
('3', 'Staff', 'Staff Member'),
('4', 'Customer', 'Regular Customer');

-- Insert test users
INSERT INTO Users (UserId, Username, FirstName, LastName, Email, PasswordHash, PhoneNumber, RoleId, Status, IsActive, CreatedDate, NormalizedUserName, NormalizedEmail, EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount, SecurityStamp, ConcurrencyStamp) VALUES 
('user-1', 'admin', 'Admin', 'User', 'admin@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456789', '1', 'Active', 1, NOW(), 'ADMIN', 'ADMIN@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-1', 'concurrency-stamp-1'),
('user-2', 'owner1', 'John', 'Doe', 'owner1@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456788', '2', 'Active', 1, NOW(), 'OWNER1', 'OWNER1@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-2', 'concurrency-stamp-2'),
('user-3', 'staff1', 'Jane', 'Smith', 'staff1@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456787', '3', 'Active', 1, NOW(), 'STAFF1', 'STAFF1@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-3', 'concurrency-stamp-3'),
('user-4', 'customer1', 'Alice', 'Johnson', 'customer1@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456786', '4', 'Active', 1, NOW(), 'CUSTOMER1', 'CUSTOMER1@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-4', 'concurrency-stamp-4'),
('user-5', 'customer2', 'Bob', 'Wilson', 'customer2@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456785', '4', 'Active', 1, NOW(), 'CUSTOMER2', 'CUSTOMER2@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-5', 'concurrency-stamp-5'),
('user-6', 'customer3', 'Carol', 'Brown', 'customer3@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456784', '4', 'Active', 1, NOW(), 'CUSTOMER3', 'CUSTOMER3@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-6', 'concurrency-stamp-6'),
('user-7', 'customer4', 'David', 'Davis', 'customer4@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456783', '4', 'Active', 1, NOW(), 'CUSTOMER4', 'CUSTOMER4@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-7', 'concurrency-stamp-7'),
('user-8', 'customer5', 'Eva', 'Miller', 'customer5@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456782', '4', 'Active', 1, NOW(), 'CUSTOMER5', 'CUSTOMER5@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-8', 'concurrency-stamp-8'),
('user-9', 'customer6', 'Frank', 'Garcia', 'customer6@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456781', '4', 'Active', 1, NOW(), 'CUSTOMER6', 'CUSTOMER6@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-9', 'concurrency-stamp-9'),
('user-10', 'customer7', 'Grace', 'Martinez', 'customer7@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456780', '4', 'Active', 1, NOW(), 'CUSTOMER7', 'CUSTOMER7@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-10', 'concurrency-stamp-10'),
('user-11', 'customer8', 'Henry', 'Anderson', 'customer8@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456779', '4', 'Active', 1, NOW(), 'CUSTOMER8', 'CUSTOMER8@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-11', 'concurrency-stamp-11'),
('user-12', 'customer9', 'Ivy', 'Taylor', 'customer9@ecodana.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '0123456778', '4', 'Active', 1, NOW(), 'CUSTOMER9', 'CUSTOMER9@ECODANA.COM', 1, 0, 0, 0, 'security-stamp-12', 'concurrency-stamp-12');
