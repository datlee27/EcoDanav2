-- =============================================
-- USER MANAGEMENT SAMPLE DATA
-- Database: ecodanangv2
-- Purpose: Populate Users and Roles tables with sample data
-- =============================================

USE ecodanangv2;

-- =============================================
-- SECTION 1: ROLES DATA
-- =============================================

-- Clear existing roles (optional - comment out if you want to keep existing data)
-- DELETE FROM Users WHERE 1=1;
-- DELETE FROM Roles WHERE 1=1;

-- Insert Roles
INSERT INTO Roles (RoleId, RoleName, NormalizedName) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Admin', 'ADMIN'),
('550e8400-e29b-41d4-a716-446655440002', 'Staff', 'STAFF'),
('550e8400-e29b-41d4-a716-446655440003', 'Customer', 'CUSTOMER'),
('550e8400-e29b-41d4-a716-446655440004', 'Owner', 'OWNER')
ON DUPLICATE KEY UPDATE 
    RoleName = VALUES(RoleName),
    NormalizedName = VALUES(NormalizedName);

-- =============================================
-- SECTION 2: USERS DATA
-- Password for all users: password123
-- Hashed using BCrypt with strength 10
-- =============================================

INSERT INTO Users (
    UserId, 
    Username, 
    FirstName, 
    LastName, 
    UserDOB, 
    PhoneNumber, 
    AvatarUrl, 
    Gender, 
    Status, 
    RoleId, 
    Email, 
    EmailVerifed, 
    PasswordHash, 
    CreatedDate, 
    NormalizedUserName, 
    NormalizedEmail, 
    SecurityStamp, 
    ConcurrencyStamp, 
    TwoFactorEnabled, 
    LockoutEnd, 
    LockoutEnabled, 
    AccessFailedCount
) VALUES
-- Admin User
(
    '650e8400-e29b-41d4-a716-446655440001',
    'admin',
    'System',
    'Administrator',
    '1990-01-15',
    '0901234567',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440001',
    'admin@ecodana.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    '2024-01-01 08:00:00',
    'ADMIN',
    'ADMIN@ECODANA.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),

-- Staff Users
(
    '650e8400-e29b-41d4-a716-446655440002',
    'staff_nguyen',
    'Nguyen Van',
    'An',
    '1995-03-20',
    '0912345678',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440002',
    'nguyen.an@ecodana.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-01-15 09:00:00',
    'STAFF_NGUYEN',
    'NGUYEN.AN@ECODANA.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440003',
    'staff_tran',
    'Tran Thi',
    'Binh',
    '1993-07-12',
    '0923456789',
    NULL,
    'Female',
    'Active',
    '550e8400-e29b-41d4-a716-446655440002',
    'tran.binh@ecodana.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-01-20 10:00:00',
    'STAFF_TRAN',
    'TRAN.BINH@ECODANA.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),

-- Owner Users
(
    '650e8400-e29b-41d4-a716-446655440004',
    'owner_le',
    'Le Van',
    'Cuong',
    '1988-05-25',
    '0934567890',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440004',
    'le.cuong@ecodana.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-02-01 11:00:00',
    'OWNER_LE',
    'LE.CUONG@ECODANA.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440005',
    'owner_pham',
    'Pham Thi',
    'Dung',
    '1992-11-08',
    '0945678901',
    NULL,
    'Female',
    'Active',
    '550e8400-e29b-41d4-a716-446655440004',
    'pham.dung@ecodana.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-02-05 12:00:00',
    'OWNER_PHAM',
    'PHAM.DUNG@ECODANA.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),

-- Customer Users
(
    '650e8400-e29b-41d4-a716-446655440006',
    'customer_hoang',
    'Hoang Van',
    'Em',
    '1998-02-14',
    '0956789012',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440003',
    'hoang.em@gmail.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-03-01 13:00:00',
    'CUSTOMER_HOANG',
    'HOANG.EM@GMAIL.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440007',
    'customer_vo',
    'Vo Thi',
    'Fang',
    '1997-09-22',
    '0967890123',
    NULL,
    'Female',
    'Active',
    '550e8400-e29b-41d4-a716-446655440003',
    'vo.fang@gmail.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-03-05 14:00:00',
    'CUSTOMER_VO',
    'VO.FANG@GMAIL.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440008',
    'customer_do',
    'Do Van',
    'Giang',
    '1999-06-30',
    '0978901234',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440003',
    'do.giang@gmail.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-03-10 15:00:00',
    'CUSTOMER_DO',
    'DO.GIANG@GMAIL.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440009',
    'customer_bui',
    'Bui Thi',
    'Huong',
    '1996-12-18',
    '0989012345',
    NULL,
    'Female',
    'Active',
    '550e8400-e29b-41d4-a716-446655440003',
    'bui.huong@gmail.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-03-15 16:00:00',
    'CUSTOMER_BUI',
    'BUI.HUONG@GMAIL.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),
(
    '650e8400-e29b-41d4-a716-446655440010',
    'customer_dang',
    'Dang Van',
    'Inh',
    '2000-04-05',
    '0990123456',
    NULL,
    'Male',
    'Active',
    '550e8400-e29b-41d4-a716-446655440003',
    'dang.inh@gmail.com',
    1,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-03-20 17:00:00',
    'CUSTOMER_DANG',
    'DANG.INH@GMAIL.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),

-- Inactive User (for testing)
(
    '650e8400-e29b-41d4-a716-446655440011',
    'inactive_user',
    'Inactive',
    'User',
    '1994-08-10',
    '0901111111',
    NULL,
    'Other',
    'Inactive',
    '550e8400-e29b-41d4-a716-446655440003',
    'inactive@example.com',
    0,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-02-01 18:00:00',
    'INACTIVE_USER',
    'INACTIVE@EXAMPLE.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    0,
    0
),

-- Banned User (for testing)
(
    '650e8400-e29b-41d4-a716-446655440012',
    'banned_user',
    'Banned',
    'User',
    '1991-03-28',
    '0902222222',
    NULL,
    'Male',
    'Banned',
    '550e8400-e29b-41d4-a716-446655440003',
    'banned@example.com',
    0,
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '2024-01-15 19:00:00',
    'BANNED_USER',
    'BANNED@EXAMPLE.COM',
    UUID(),
    UUID(),
    0,
    NULL,
    1,
    5
)
ON DUPLICATE KEY UPDATE 
    Username = VALUES(Username),
    FirstName = VALUES(FirstName),
    LastName = VALUES(LastName),
    Email = VALUES(Email);

-- =============================================
-- VERIFICATION QUERIES
-- =============================================

-- Count users by role
SELECT 
    r.RoleName,
    COUNT(u.UserId) as UserCount
FROM Roles r
LEFT JOIN Users u ON r.RoleId = u.RoleId
GROUP BY r.RoleId, r.RoleName
ORDER BY r.RoleName;

-- Count users by status
SELECT 
    Status,
    COUNT(*) as UserCount
FROM Users
GROUP BY Status
ORDER BY Status;

-- List all users with their roles
SELECT 
    u.Username,
    u.Email,
    CONCAT(u.FirstName, ' ', u.LastName) as FullName,
    r.RoleName,
    u.Status,
    u.CreatedDate
FROM Users u
LEFT JOIN Roles r ON u.RoleId = r.RoleId
ORDER BY u.CreatedDate DESC;

-- =============================================
-- NOTES
-- =============================================
-- Default password for all users: password123
-- 
-- User Distribution:
-- - 1 Admin
-- - 2 Staff
-- - 2 Owners
-- - 7 Customers (5 active, 1 inactive, 1 banned)
-- Total: 12 users
--
-- To generate new BCrypt password hash:
-- Use online tool or Spring Security BCryptPasswordEncoder
-- Example in Java:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hash = encoder.encode("password123");
-- =============================================
