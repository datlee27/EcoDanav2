-- Script to add Owner role and user for EvoDana
USE ecodanav2;

-- 1. Add Owner role if it doesn't exist
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('owner-role-id', 'Owner', 'OWNER');

-- 2. Add Owner user
INSERT IGNORE INTO Users (
    UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName,
    RoleId, Status, CreatedDate, NormalizedUserName, NormalizedEmail,
    EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount,
    SecurityStamp, ConcurrencyStamp
) VALUES (
    'owner-user-id-001',
    'owner',
    'owner@test.com',
    '$2a$10$udCgxkgXEJkPLNts47c.vevinawbk2CTSbzhydw3mRP53Eq7mSiri', -- password: owner123
    '0123456789',
    'Owner',
    'Manager',
    'owner-role-id',
    'Active',
    NOW(),
    'OWNER',
    'OWNER@TEST.COM',
    1,
    0,
    0,
    0,
    'owner-security-stamp-001',
    'owner-concurrency-stamp-001'
);

-- 3. Verify the user was created
SELECT 
    u.UserId,
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE u.Email = 'owner@test.com';

-- 4. Show all roles
SELECT * FROM Roles ORDER BY RoleName;

-- 5. Show all users with their roles
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
ORDER BY r.RoleName, u.Username;
