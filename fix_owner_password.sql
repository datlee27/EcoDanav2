-- Fix Owner password and check users
USE ecodanav2;

-- 1. Check current Owner users
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status,
    LEFT(u.PasswordHash, 20) as 'PasswordHash_Preview'
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE r.RoleName = 'Owner' OR u.Email LIKE '%owner%';

-- 2. Update password for owner123@test.com (if exists)
UPDATE Users 
SET PasswordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'  -- owner123
WHERE Email = 'owner123@test.com';

-- 3. Update password for owner@test.com (if exists)  
UPDATE Users 
SET PasswordHash = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'  -- owner123
WHERE Email = 'owner@test.com';

-- 4. If no Owner users exist, create one
INSERT IGNORE INTO Users (
    UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName,
    RoleId, Status, CreatedDate, NormalizedUserName, NormalizedEmail,
    EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount,
    SecurityStamp, ConcurrencyStamp
) VALUES (
    'owner-user-id-002',
    'owner123',
    'owner123@test.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- owner123
    '0987654321',
    'Owner',
    'Test',
    'owner-role-id',
    'Active',
    NOW(),
    'OWNER123',
    'OWNER123@TEST.COM',
    1, 0, 0, 0,
    'owner-security-stamp-002',
    'owner-concurrency-stamp-002'
);

-- 5. Verify the user
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE u.Email = 'owner123@test.com';
