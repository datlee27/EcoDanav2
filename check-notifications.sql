-- Check notifications in database
SELECT 
    n.NotificationId,
    n.UserId,
    u.Username,
    r.RoleName,
    n.Message,
    n.IsRead,
    n.CreatedDate,
    n.RelatedId,
    n.NotificationType
FROM Notification n
LEFT JOIN Users u ON n.UserId = u.UserId
LEFT JOIN Roles r ON u.RoleId = r.RoleId
ORDER BY n.CreatedDate DESC
LIMIT 10;

-- Count notifications by user
SELECT 
    u.Username,
    r.RoleName,
    COUNT(*) as TotalNotifications,
    SUM(CASE WHEN n.IsRead = 0 THEN 1 ELSE 0 END) as UnreadCount
FROM Notification n
LEFT JOIN Users u ON n.UserId = u.UserId
LEFT JOIN Roles r ON u.RoleId = r.RoleId
GROUP BY u.Username, r.RoleName;

-- Check admin users
SELECT 
    u.UserId,
    u.Username,
    u.Email,
    r.RoleName
FROM Users u
JOIN Roles r ON u.RoleId = r.RoleId
WHERE r.RoleName = 'ADMIN';
