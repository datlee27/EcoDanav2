-- Fix Owner role assignment
USE ecodanav2;

-- 1. Check current Owner users and their roles
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.RoleId,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE u.Email LIKE '%owner%';

-- 2. Check if owner-role-id exists
SELECT * FROM Roles WHERE RoleName = 'Owner';

-- 3. If owner-role-id doesn't exist, create it
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('owner-role-id', 'Owner', 'OWNER');

-- 4. Update owner123@test.com to use Owner role
UPDATE Users 
SET RoleId = 'owner-role-id'
WHERE Email = 'owner123@test.com';

-- 5. Update owner@test.com to use Owner role (if exists)
UPDATE Users 
SET RoleId = 'owner-role-id'
WHERE Email = 'owner@test.com';

-- 6. Verify the changes
SELECT 
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    r.RoleName,
    u.RoleId,
    u.Status
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE u.Email LIKE '%owner%';

-- 7. Show all roles
SELECT * FROM Roles ORDER BY RoleName;
