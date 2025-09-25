-- Check Owner users in database
USE ecodanav2;

-- 1. Check all users with Owner role
SELECT 
    u.UserId,
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status,
    u.PasswordHash
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE r.RoleName = 'Owner';

-- 2. Check if owner-role-id exists
SELECT * FROM Roles WHERE RoleName = 'Owner';

-- 3. Check all users with owner in email
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE u.Email LIKE '%owner%';

-- 4. Test password hash for owner123
-- This should match the hash in database
SELECT '$2a$10$udCgxkgXEJkPLNts47c.vevinawbk2CTSbzhydw3mRP53Eq7mSiri' as 'Expected Hash';
