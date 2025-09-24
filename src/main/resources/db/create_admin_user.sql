-- Create admin user directly in database
USE ecodanav2;

-- Insert roles if they don't exist
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('admin-role-id', 'Admin', 'ADMIN'),
('staff-role-id', 'Staff', 'STAFF'),
('customer-role-id', 'Customer', 'CUSTOMER');

-- Create admin user with simple password
INSERT INTO Users (
    UserId, Username, UserDOB, PhoneNumber, AvatarUrl, Gender, 
    FirstName, LastName, Status, RoleId, CreatedDate, NormalizedUserName, 
    Email, NormalizedEmail, EmailVerifed, PasswordHash, SecurityStamp, 
    ConcurrencyStamp, TwoFactorEnabled, LockoutEnd, LockoutEnabled, AccessFailedCount
) VALUES (
    'admin-001', 
    'admin', 
    '1990-01-01', 
    '0123456789', 
    NULL, 
    'Male', 
    'Admin', 
    'User', 
    'Active', 
    'admin-role-id', 
    NOW(), 
    'ADMIN', 
    'admin@ecodana.com', 
    'ADMIN@ECODANA.COM', 
    1, 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- password: admin123
    'admin-stamp', 
    'admin-concurrency', 
    0, 
    NULL, 
    1, 
    0
) ON DUPLICATE KEY UPDATE
    PasswordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi',
    Status = 'Active';

-- Create staff user
INSERT INTO Users (
    UserId, Username, UserDOB, PhoneNumber, AvatarUrl, Gender, 
    FirstName, LastName, Status, RoleId, CreatedDate, NormalizedUserName, 
    Email, NormalizedEmail, EmailVerifed, PasswordHash, SecurityStamp, 
    ConcurrencyStamp, TwoFactorEnabled, LockoutEnd, LockoutEnabled, AccessFailedCount
) VALUES (
    'staff-001', 
    'owner', 
    '1985-05-15', 
    '0987654321', 
    NULL, 
    'Female', 
    'Owner', 
    'Manager', 
    'Active', 
    'staff-role-id', 
    NOW(), 
    'OWNER', 
    'owner@ecodana.com', 
    'OWNER@ECODANA.COM', 
    1, 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- password: admin123
    'owner-stamp', 
    'owner-concurrency', 
    0, 
    NULL, 
    1, 
    0
) ON DUPLICATE KEY UPDATE
    PasswordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi',
    Status = 'Active';

-- Create customer user
INSERT INTO Users (
    UserId, Username, UserDOB, PhoneNumber, AvatarUrl, Gender, 
    FirstName, LastName, Status, RoleId, CreatedDate, NormalizedUserName, 
    Email, NormalizedEmail, EmailVerifed, PasswordHash, SecurityStamp, 
    ConcurrencyStamp, TwoFactorEnabled, LockoutEnd, LockoutEnabled, AccessFailedCount
) VALUES (
    'customer-001', 
    'customer', 
    '1995-08-20', 
    '0555555555', 
    NULL, 
    'Male', 
    'John', 
    'Doe', 
    'Active', 
    'customer-role-id', 
    NOW(), 
    'CUSTOMER', 
    'customer@ecodana.com', 
    'CUSTOMER@ECODANA.COM', 
    1, 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- password: admin123
    'customer-stamp', 
    'customer-concurrency', 
    0, 
    NULL, 
    1, 
    0
) ON DUPLICATE KEY UPDATE
    PasswordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi',
    Status = 'Active';

COMMIT;
