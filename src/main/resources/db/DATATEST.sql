USE ecodana;
show tables;
-- 1. Chèn dữ liệu vào bảng Roles
INSERT INTO Roles (RoleId, RoleName, NormalizedName) VALUES
(UUID(), 'Admin', 'ADMIN'),
(UUID(), 'Staff', 'STAFF'),
(UUID(), 'Customer', 'CUSTOMER');

-- 2. Chèn dữ liệu vào bảng Users (lấy RoleId từ bảng Roles)
INSERT INTO Users (UserId, Username, UserDOB, PhoneNumber, AvatarUrl, Gender, FirstName, LastName, Status, RoleId, CreatedDate, Email, NormalizedEmail, EmailVerifed, PasswordHash, TwoFactorEnabled, LockoutEnabled, AccessFailedCount) VALUES
(UUID(), 'admin01', '1985-05-15', '0901234567', 'https://example.com/avatars/admin.jpg', 'Male', 'Nguyễn', 'Văn Admin', 'Active', (SELECT RoleId FROM Roles WHERE RoleName = 'Admin'), NOW(), 'admin@evodanang.com', 'ADMIN@EVODANANG.COM', 1, '$2a$10$rOzJq/w5U6b/2V2Qd6zZ0eJZ6J6J6J6J6J6J6J6J6J6J6J6J6J6J6', 0, 0, 0),
(UUID(), 'staff01', '1990-08-20', '0907654321', 'https://example.com/avatars/staff.jpg', 'Female', 'Trần', 'Thị Staff', 'Active', (SELECT RoleId FROM Roles WHERE RoleName = 'Staff'), NOW(), 'staff@evodanang.com', 'STAFF@EVODANANG.COM', 1, '$2a$10$rOzJq/w5U6b/2V2Qd6zZ0eJZ6J6J6J6J6J6J6J6J6J6J6J6J6J6', 0, 0, 0),
(UUID(), 'customer01', '1995-12-10', '0912345678', 'https://example.com/avatars/customer1.jpg', 'Male', 'Lê', 'Văn Customer', 'Active', (SELECT RoleId FROM Roles WHERE RoleName = 'Customer'), NOW(), 'customer1@example.com', 'CUSTOMER1@EXAMPLE.COM', 1, '$2a$10$rOzJq/w5U6b/2V2Qd6zZ0eJZ6J6J6J6J6J6J6J6J6J6J6J6J6J6', 0, 0, 0);

-- 3. Chèn dữ liệu vào bảng CarBrand
INSERT INTO CarBrand (BrandId, BrandName) VALUES
(UUID(), 'VinFast'),
(UUID(), 'Tesla'),
(UUID(), 'Honda'),
(UUID(), 'Yamaha');

-- 4. Chèn dữ liệu vào bảng FuelType
INSERT INTO FuelType (FuelTypeId, FuelName) VALUES
(UUID(), 'Electric'),
(UUID(), 'Gasoline');

-- 5. Chèn dữ liệu vào bảng TransmissionType
INSERT INTO TransmissionType (TransmissionTypeId, TransmissionName) VALUES
(UUID(), 'Automatic'),
(UUID(), 'Manual');

-- 6. Chèn dữ liệu vào bảng VehicleCategories
INSERT INTO VehicleCategories (CategoryId, CategoryName, VehicleType) VALUES
(UUID(), 'Sedan', 'ElectricCar'),
(UUID(), 'SUV', 'ElectricCar'),
(UUID(), 'Sport Bike', 'ElectricMotorcycle'),
(UUID(), 'Scooter', 'ElectricMotorcycle');

-- 7. Chèn dữ liệu vào bảng CarFeature
INSERT INTO CarFeature (FeatureId, FeatureName) VALUES
(UUID(), 'Air Conditioning'),
(UUID(), 'Navigation System'),
(UUID(), 'Bluetooth'),
(UUID(), 'Backup Camera'),
(UUID(), 'Fast Charging');

-- 8. Chèn dữ liệu vào bảng Vehicle
INSERT INTO Vehicle (VehicleId, BrandId, VehicleModel, YearManufactured, TransmissionTypeId, FuelTypeId, LicensePlate, Seats, Odometer, PricePerHour, PricePerDay, PricePerMonth, Status, Description, CategoryId, VehicleType, RequiresLicense, BatteryCapacity) VALUES
(
  UUID(), 
  (SELECT BrandId FROM CarBrand WHERE BrandName = 'VinFast'), 
  'VF e34', 
  2023, 
  (SELECT TransmissionTypeId FROM TransmissionType WHERE TransmissionName = 'Automatic'), 
  (SELECT FuelTypeId FROM FuelType WHERE FuelName = 'Electric'), 
  '43A-12345', 
  5, 
  5000, 
  100000, 
  800000, 
  20000000, 
  'Available', 
  'Xe điện VinFast VF e34 đời mới', 
  (SELECT CategoryId FROM VehicleCategories WHERE CategoryName = 'Sedan'), 
  'ElectricCar', 
  1, 
  42.0
),
(
  UUID(), 
  (SELECT BrandId FROM CarBrand WHERE BrandName = 'Honda'), 
  'SH Mode', 
  2023, 
  (SELECT TransmissionTypeId FROM TransmissionType WHERE TransmissionName = 'Automatic'), 
  (SELECT FuelTypeId FROM FuelType WHERE FuelName = 'Electric'), 
  '43A-67890', 
  2, 
  3000, 
  50000, 
  400000, 
  10000000, 
  'Available', 
  'Xe tay điện Honda SH Mode', 
  (SELECT CategoryId FROM VehicleCategories WHERE CategoryName = 'Scooter'), 
  'ElectricMotorcycle', 
  1, 
  5.0
);

-- 9. Chèn dữ liệu vào bảng VehicleImages
INSERT INTO VehicleImages (ImageId, VehicleId, ImageUrl, IsMain) VALUES
(UUID(), (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'), 'https://example.com/vehicles/vfe34_1.jpg', 1),
(UUID(), (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'), 'https://example.com/vehicles/vfe34_2.jpg', 0),
(UUID(), (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-67890'), 'https://example.com/vehicles/sh_mode_1.jpg', 1);

-- 10. Chèn dữ liệu vào bảng VehicleFeaturesMapping
INSERT INTO VehicleFeaturesMapping (VehicleId, FeatureId) VALUES
(
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT FeatureId FROM CarFeature WHERE FeatureName = 'Air Conditioning')
),
(
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT FeatureId FROM CarFeature WHERE FeatureName = 'Navigation System')
),
(
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-67890'),
  (SELECT FeatureId FROM CarFeature WHERE FeatureName = 'Fast Charging')
);

-- 11. Chèn dữ liệu vào bảng Discount
INSERT INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, CreatedDate, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory) VALUES
(UUID(), 'Khuyến mãi mùa hè', 'Giảm giá cho dịch vụ thuê xe mùa hè', 'Percentage', 10.00, '2024-06-01', '2024-08-31', 1, NOW(), 'SUMMER2024', 500000, 200000, 100, 0, 'Seasonal'),
(UUID(), 'Khách hàng mới', 'Giảm giá cho khách hàng đăng ký lần đầu', 'Fixed', 100000.00, '2024-01-01', '2024-12-31', 1, NOW(), 'NEWUSER', 300000, 100000, 500, 0, 'NewCustomer');

-- 12. Chèn dữ liệu vào bảng Insurance
INSERT INTO Insurance (InsuranceId, InsuranceName, InsuranceType, BaseRatePerDay, PercentageRate, CoverageAmount, ApplicableVehicleSeats, Description, IsActive, CreatedDate) VALUES
(UUID(), 'Bảo hiểm cơ bản', 'Basic', 50000.00, 5.00, 50000000.00, '1-5', 'Bảo hiểm cơ bản cho xe dưới 5 chỗ', 1, NOW()),
(UUID(), 'Bảo hiểm cao cấp', 'Premium', 100000.00, 8.00, 100000000.00, '1-7', 'Bảo hiểm cao cấp cho mọi loại xe', 1, NOW());

-- 13. Chèn dữ liệu vào bảng Terms
INSERT INTO Terms (TermsId, Version, Title, ShortContent, FullContent, EffectiveDate, IsActive, CreatedDate) VALUES
(UUID(), 'v1.0', 'Điều khoản dịch vụ', 'Điều khoản và điều kiện sử dụng dịch vụ', 'Nội dung đầy đủ của điều khoản dịch vụ...', '2024-01-01', 1, NOW());

-- 14. Chèn dữ liệu vào bảng Booking
INSERT INTO Booking (BookingId, UserId, VehicleId, PickupDateTime, ReturnDateTime, TotalAmount, Status, DiscountId, BookingCode, RentalType, CustomerName, CustomerPhone, CustomerEmail, TermsAgreed, TermsAgreedAt, TermsVersion) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  NOW(),
  DATE_ADD(NOW(), INTERVAL 3 DAY),
  2400000,
  'Confirmed',
  (SELECT DiscountId FROM Discount WHERE VoucherCode = 'SUMMER2024'),
  'BOOK001',
  'daily',
  'Lê Văn Customer',
  '0912345678',
  'customer1@example.com',
  1,
  NOW(),
  'v1.0'
);

-- 15. Chèn dữ liệu vào bảng BookingApproval
INSERT INTO BookingApproval (ApprovalId, BookingId, StaffId, ApprovalStatus, ApprovalDate, Note) VALUES
(
  UUID(),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  (SELECT UserId FROM Users WHERE Username = 'staff01'),
  'Approved',
  NOW(),
  'Đơn đặt xe đã được duyệt'
);

-- 16. Chèn dữ liệu vào bảng BookingInsurance
INSERT INTO BookingInsurance (BookingInsuranceId, BookingId, InsuranceId, PremiumAmount, RentalDays, VehicleSeats, EstimatedVehicleValue, CreatedAt) VALUES
(
  UUID(),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  (SELECT InsuranceId FROM Insurance WHERE InsuranceName = 'Bảo hiểm cơ bản'),
  150000,
  3,
  5,
  500000000,
  NOW()
);

-- 17. Chèn dữ liệu vào bảng Contract
INSERT INTO Contract (ContractId, ContractCode, UserId, BookingId, Status, TermsAccepted, SignatureData, SignedDate) VALUES
(
  UUID(),
  'CONT001',
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  'Signed',
  1,
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...',
  NOW()
);

-- 18. Chèn dữ liệu vào bảng ContractDocuments
INSERT INTO ContractDocuments (DocumentId, ContractId, DriverLicenseImageUrl, DriverLicenseNumber, CitizenIdFrontImageUrl, CitizenIdBackImageUrl, CitizenIdNumber, CitizenIdIssuedDate, CitizenIdIssuedPlace) VALUES
(
  UUID(),
  (SELECT ContractId FROM Contract WHERE ContractCode = 'CONT001'),
  'https://example.com/documents/driver_license.jpg',
  '123456789',
  'https://example.com/documents/citizen_front.jpg',
  'https://example.com/documents/citizen_back.jpg',
  '00123456789',
  '2020-01-01',
  'Cục Cảnh sát Đà Nẵng'
);

-- 19. Chèn dữ liệu vào bảng Payment
INSERT INTO Payment (PaymentId, BookingId, ContractId, Amount, PaymentMethod, PaymentStatus, PaymentType, TransactionId, PaymentDate, UserId, Notes) VALUES
(
  UUID(),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  (SELECT ContractId FROM Contract WHERE ContractCode = 'CONT001'),
  2400000,
  'CreditCard',
  'Completed',
  'Full',
  'TXN001',
  NOW(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  'Thanh toán đầy đủ cho đơn đặt xe BOOK001'
);

-- 20. Chèn dữ liệu vào bảng BatteryLogs
INSERT INTO BatteryLogs (LogId, VehicleId, BookingId, BatteryLevel, CheckTime, Note) VALUES
(
  UUID(),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  85.50,
  NOW(),
  'Pin đầy trước khi giao xe'
);

-- 21. Chèn dữ liệu vào bảng VehicleConditionLogs
INSERT INTO VehicleConditionLogs (LogId, BookingId, VehicleId, StaffId, CheckType, Odometer, FuelLevel, ConditionStatus, ConditionDescription, DamageImages, Note) VALUES
(
  UUID(),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT UserId FROM Users WHERE Username = 'staff01'),
  'CheckOut',
  5000,
  'Full',
  'Good',
  'Xe trong tình trạng tốt, không vết trầy xước',
  'https://example.com/damage/none.jpg',
  'Giao xe cho khách hàng'
);

-- 22. Chèn dữ liệu vào bảng UserFeedback
INSERT INTO UserFeedback (FeedbackId, UserId, VehicleId, BookingId, Rating, Content, Reviewed, CreatedDate, StaffReply) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  5,
  'Xe chạy êm, dịch vụ tốt, nhân viên nhiệt tình',
  CURDATE(),
  NOW(),
  'Cảm ơn bạn đã phản hồi. Chúng tôi rất vui vì bạn hài lòng với dịch vụ.'
);

-- 23. Chèn dữ liệu vào bảng DriverLicenses
INSERT INTO DriverLicenses (LicenseId, UserId, LicenseNumber, FullName, DOB, LicenseImage, CreatedDate) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  '123456789',
  'Lê Văn Customer',
  '1995-12-10',
  'https://example.com/documents/driver_license_full.jpg',
  NOW()
);

-- 24. Chèn dữ liệu vào bảng CitizenIdCards
INSERT INTO CitizenIdCards (Id, UserId, CitizenIdNumber, FullName, DOB, CitizenIdImageUrl, CitizenIdBackImageUrl, CitizenIdIssuedDate, CitizenIdIssuedPlace, CreatedDate) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  '00123456789',
  'Lê Văn Customer',
  '1995-12-10',
  'https://example.com/documents/citizen_front.jpg',
  'https://example.com/documents/citizen_back.jpg',
  '2020-01-01',
  'Cục Cảnh sát Đà Nẵng',
  NOW()
);

-- 25. Chèn dữ liệu vào bảng UserFavoriteVehicles
INSERT INTO UserFavoriteVehicles (UserId, VehicleId) VALUES
(
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345')
);

-- 26. Chèn dữ liệu vào bảng Notification
INSERT INTO Notification (NotificationId, UserId, Message, CreatedDate, IsRead) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  'Đơn đặt xe BOOK001 của bạn đã được xác nhận',
  NOW(),
  0
);

-- 27. Chèn dữ liệu vào bảng AccountDeletionLogs
INSERT INTO AccountDeletionLogs (LogId, UserId, DeletionReason, AdditionalComments) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  'Không sử dụng dịch vụ nữa',
  'Khách hàng yêu cầu xóa tài khoản'
);

-- 28. Tạo booking không có payment (Status = Pending thường không cần payment ngay)
INSERT INTO Booking (BookingId, UserId, VehicleId, PickupDateTime, ReturnDateTime, TotalAmount, Status, BookingCode, RentalType, CustomerName, CustomerPhone, CustomerEmail, TermsAgreed, TermsVersion) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-67890'),
  DATE_ADD(NOW(), INTERVAL 5 DAY),
  DATE_ADD(NOW(), INTERVAL 7 DAY),
  800000,
  'Pending',
  'BOOK002',
  'daily',
  'Lê Văn Customer',
  '0912345678',
  'customer1@example.com',
  1,
  'v1.0'
);

-- 29. Tạo vehicle không có image
INSERT INTO Vehicle (VehicleId, BrandId, VehicleModel, YearManufactured, TransmissionTypeId, FuelTypeId, LicensePlate, Seats, Odometer, PricePerHour, PricePerDay, PricePerMonth, Status, Description, CategoryId, VehicleType, RequiresLicense, BatteryCapacity) VALUES
(
  UUID(), 
  (SELECT BrandId FROM CarBrand WHERE BrandName = 'Yamaha'), 
  'NMAX', 
  2023, 
  (SELECT TransmissionTypeId FROM TransmissionType WHERE TransmissionName = 'Automatic'), 
  (SELECT FuelTypeId FROM FuelType WHERE FuelName = 'Electric'), 
  '43A-11111', 
  2, 
  2000, 
  40000, 
  300000, 
  8000000, 
  'Available', 
  'Xe điện Yamaha NMAX', 
  (SELECT CategoryId FROM VehicleCategories WHERE CategoryName = 'Scooter'), 
  'ElectricMotorcycle', 
  1, 
  4.5
);

-- 30. Tạo feedback với rating không hợp lệ (sẽ bị chặn bởi CHECK constraint nếu đã được áp dụng)
-- Lưu ý: Nếu bạn đã thêm ràng buộc CHECK, câu lệnh này sẽ FAIL
INSERT INTO UserFeedback (FeedbackId, UserId, VehicleId, BookingId, Rating, Content, Reviewed, CreatedDate) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-12345'),
  (SELECT BookingId FROM Booking WHERE BookingCode = 'BOOK001'),
  0,  -- Rating không hợp lệ
  'Feedback với rating không hợp lệ',
  CURDATE(),
  NOW()
);

-- 31. Tạo booking với TotalAmount âm (sẽ bị chặn bởi CHECK constraint nếu đã được áp dụng)
-- Lưu ý: Nếu bạn đã thêm ràng buộc CHECK, câu lệnh này sẽ FAIL
INSERT INTO Booking (BookingId, UserId, VehicleId, PickupDateTime, ReturnDateTime, TotalAmount, Status, BookingCode, RentalType, CustomerName, CustomerPhone, CustomerEmail, TermsAgreed, TermsVersion) VALUES
(
  UUID(),
  (SELECT UserId FROM Users WHERE Username = 'customer01'),
  (SELECT VehicleId FROM Vehicle WHERE LicensePlate = '43A-67890'),
  DATE_ADD(NOW(), INTERVAL 10 DAY),
  DATE_ADD(NOW(), INTERVAL 12 DAY),
  -500000,  -- TotalAmount âm
  'Pending',
  'BOOK003',
  'daily',
  'Lê Văn Customer',
  '0912345678',
  'customer1@example.com',
  1,
  'v1.0'
);

---------------------------------------------------------------------------------------------------------
-- TEST DỮ LIỆU :

-- Kiểm tra user có role hợp lệ
SELECT u.UserId, u.Username, u.Email, r.RoleName 
FROM Users u 
JOIN Roles r ON u.RoleId = r.RoleId;


-- Kiểm tra booking có user và vehicle hợp lệ
SELECT b.BookingCode, u.Username, v.LicensePlate, v.VehicleModel, b.Status
FROM Booking b
JOIN Users u ON b.UserId = u.UserId
JOIN Vehicle v ON b.VehicleId = v.VehicleId;


-- Kiểm tra thông tin đầy đủ của vehicle
SELECT v.LicensePlate, cb.BrandName, ft.FuelName, tt.TransmissionName, vc.CategoryName
FROM Vehicle v
JOIN CarBrand cb ON v.BrandId = cb.BrandId
JOIN FuelType ft ON v.FuelTypeId = ft.FuelTypeId
LEFT JOIN TransmissionType tt ON v.TransmissionTypeId = tt.TransmissionTypeId
LEFT JOIN VehicleCategories vc ON v.CategoryId = vc.CategoryId;


-- Kiểm tra booking sử dụng discount
SELECT b.BookingCode, d.DiscountName, d.VoucherCode, b.TotalAmount
FROM Booking b
LEFT JOIN Discount d ON b.DiscountId = d.DiscountId;


-- Kiểm tra bảo hiểm của booking
SELECT b.BookingCode, i.InsuranceName, bi.PremiumAmount, bi.RentalDays
FROM Booking b
JOIN BookingInsurance bi ON b.BookingId = bi.BookingId
JOIN Insurance i ON bi.InsuranceId = i.InsuranceId;


-- Kiểm tra hợp đồng và thông tin liên quan
SELECT c.ContractCode, b.BookingCode, u.Username, c.Status, c.SignedDate
FROM Contract c
JOIN Booking b ON c.BookingId = b.BookingId
JOIN Users u ON c.UserId = u.UserId;


-- Kiểm tra thanh toán
SELECT p.PaymentId, b.BookingCode, c.ContractCode, p.Amount, p.PaymentStatus, p.PaymentMethod
FROM Payment p
JOIN Booking b ON p.BookingId = b.BookingId
LEFT JOIN Contract c ON p.ContractId = c.ContractId;


-- Kiểm tra tính năng của vehicle
SELECT v.LicensePlate, cf.FeatureName
FROM Vehicle v
JOIN VehicleFeaturesMapping vfm ON v.VehicleId = vfm.VehicleId
JOIN CarFeature cf ON vfm.FeatureId = cf.FeatureId
ORDER BY v.LicensePlate;


-- Kiểm tra feedback
SELECT u.Username, v.LicensePlate, b.BookingCode, uf.Rating, uf.Content
FROM UserFeedback uf
JOIN Users u ON uf.UserId = u.UserId
LEFT JOIN Vehicle v ON uf.VehicleId = v.VehicleId
LEFT JOIN Booking b ON uf.BookingId = b.BookingId;


-- Kiểm tra phê duyệt booking
SELECT b.BookingCode, u.Username as Customer, s.Username as Staff, ba.ApprovalStatus, ba.ApprovalDate
FROM BookingApproval ba
JOIN Booking b ON ba.BookingId = b.BookingId
JOIN Users u ON b.UserId = u.UserId
JOIN Users s ON ba.StaffId = s.UserId;


-- Kiểm tra log pin
SELECT v.LicensePlate, b.BookingCode, bl.BatteryLevel, bl.CheckTime, bl.Note
FROM BatteryLogs bl
JOIN Vehicle v ON bl.VehicleId = v.VehicleId
LEFT JOIN Booking b ON bl.BookingId = b.BookingId;


-- Kiểm tra log tình trạng xe
SELECT v.LicensePlate, b.BookingCode, s.Username as Staff, vcl.CheckType, vcl.ConditionStatus
FROM VehicleConditionLogs vcl
JOIN Vehicle v ON vcl.VehicleId = v.VehicleId
JOIN Booking b ON vcl.BookingId = b.BookingId
LEFT JOIN Users s ON vcl.StaffId = s.UserId;


-- Kiểm tra xe yêu thích của user
SELECT u.Username, v.LicensePlate, v.VehicleModel
FROM UserFavoriteVehicles ufv
JOIN Users u ON ufv.UserId = u.UserId
JOIN Vehicle v ON ufv.VehicleId = v.VehicleId;


-- Kiểm tra bằng lái xe
SELECT u.Username, dl.LicenseNumber, dl.FullName, dl.DOB
FROM DriverLicenses dl
JOIN Users u ON dl.UserId = u.UserId;


-- Kiểm tra CMND/CCCD
SELECT u.Username, cic.CitizenIdNumber, cic.FullName, cic.DOB
FROM CitizenIdCards cic
JOIN Users u ON cic.UserId = u.UserId;


-- Kiểm tra các booking không có payment (nên có ít nhất 1 payment)
SELECT b.BookingCode, b.Status, COUNT(p.PaymentId) as PaymentCount
FROM Booking b
LEFT JOIN Payment p ON b.BookingId = p.BookingId
GROUP BY b.BookingId, b.BookingCode, b.Status
HAVING COUNT(p.PaymentId) = 0;

-- Kiểm tra các vehicle không có image
SELECT v.LicensePlate, v.VehicleModel, COUNT(vi.ImageId) as ImageCount
FROM Vehicle v
LEFT JOIN VehicleImages vi ON v.VehicleId = vi.VehicleId
GROUP BY v.VehicleId, v.LicensePlate, v.VehicleModel
HAVING COUNT(vi.ImageId) = 0;


-- Kiểm tra rating trong UserFeedback có nằm trong khoảng 1-5
SELECT FeedbackId, Rating 
FROM UserFeedback 
WHERE Rating < 1 OR Rating > 5;

-- Kiểm tra TotalAmount trong Booking có âm không
SELECT BookingId, BookingCode, TotalAmount 
FROM Booking 
WHERE TotalAmount < 0;


-- Kiểm tra hiệu suất truy vấn sử dụng index
EXPLAIN SELECT * FROM Vehicle WHERE Status = 'Available';
EXPLAIN SELECT * FROM Booking WHERE PickupDateTime > '2024-01-01';
EXPLAIN SELECT * FROM Users WHERE Email = 'customer1@example.com';