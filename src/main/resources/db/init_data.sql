
INSERT INTO Users (UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName, Status, RoleId, CreatedDate, NormalizedUserName, NormalizedEmail, EmailVerifed, SecurityStamp, ConcurrencyStamp, TwoFactorEnabled, LockoutEnabled, AccessFailedCount) VALUES 
('550e8400-e29b-41d4-a716-446655440100', 'admin', 'admin@ecodana.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0123456789', 'Admin', 'EvoDana', 'Active', '550e8400-e29b-41d4-a716-446655440001', NOW(), 'ADMIN', 'ADMIN@ECODANA.COM', 1, 'admin-security-stamp', 'admin-concurrency-stamp', 0, 0, 0),
('550e8400-e29b-41d4-a716-446655440101', 'staff', 'staff@ecodana.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0987654321', 'Staff', 'EvoDana', 'Active', '550e8400-e29b-41d4-a716-446655440002', NOW(), 'STAFF', 'STAFF@ECODANA.COM', 1, 'staff-security-stamp', 'staff-concurrency-stamp', 0, 0, 0),
('550e8400-e29b-41d4-a716-446655440102', 'owner', 'owner@ecodana.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0912345678', 'Owner', 'EvoDana', 'Active', '550e8400-e29b-41d4-a716-446655440003', NOW(), 'OWNER', 'OWNER@ECODANA.COM', 1, 'owner-security-stamp', 'owner-concurrency-stamp', 0, 0, 0),
('550e8400-e29b-41d4-a716-446655440103', 'customer', 'customer@ecodana.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '0901234567', 'Customer', 'EvoDana', 'Active', '550e8400-e29b-41d4-a716-446655440004', NOW(), 'CUSTOMER', 'CUSTOMER@ECODANA.COM', 1, 'customer-security-stamp', 'customer-concurrency-stamp', 0, 0, 0);






-- pass all :  password  