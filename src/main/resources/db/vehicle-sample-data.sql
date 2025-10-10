-- =============================================
-- SAMPLE DATA FOR VEHICLE MANAGEMENT SYSTEM
-- EcoDana - Electric Vehicle Rental Platform
-- =============================================

-- Insert Vehicle Categories
INSERT INTO VehicleCategories (CategoryId, CategoryName) VALUES
(1, 'Sedan'),
(2, 'SUV'),
(3, 'Hatchback'),
(4, 'Scooter'),
(5, 'Sport Bike');

-- Insert Transmission Types
INSERT INTO TransmissionTypes (TransmissionTypeId, TransmissionTypeName) VALUES
(1, 'Automatic'),
(2, 'Manual'),
(3, 'CVT'),
(4, 'Single Speed');

-- Insert Sample Vehicles
INSERT INTO Vehicle (VehicleId, VehicleModel, YearManufactured, LicensePlate, Seats, Odometer, 
                     RentalPrices, Status, Description, VehicleType, RequiresLicense, 
                     BatteryCapacity, CreatedDate, CategoryId, TransmissionTypeId, MainImageUrl, 
                     ImageUrls, Features)
VALUES
-- Electric Cars
(UUID(), 'VinFast VF8', 2023, '43A-12345', 5, 5000,
 '{"hourly": 80000, "daily": 800000, "monthly": 20000000}',
 'Available',
 'SUV điện cao cấp với công nghệ hiện đại, phù hợp cho gia đình và du lịch đường dài.',
 'ElectricCar', 1, 87.7, NOW(), 2, 4,
 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800',
 '["https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400", "https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400"]',
 '["GPS", "Camera 360", "Cảm biến lùi", "Hệ thống âm thanh cao cấp", "Sạc nhanh"]'),

(UUID(), 'VinFast VF9', 2023, '43B-67890', 7, 3000,
 '{"hourly": 100000, "daily": 1000000, "monthly": 25000000}',
 'Available',
 'SUV điện 7 chỗ sang trọng, động cơ mạnh mẽ, phù hợp cho gia đình đông người.',
 'ElectricCar', 1, 123.0, NOW(), 2, 4,
 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800',
 '["https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400"]',
 '["GPS", "Camera 360", "Cảm biến lùi", "Ghế massage", "Sạc nhanh", "Hệ thống giải trí"]'),

(UUID(), 'Tesla Model 3', 2022, '30A-11111', 5, 15000,
 '{"hourly": 90000, "daily": 900000, "monthly": 22000000}',
 'Available',
 'Sedan điện thể thao với khả năng tăng tốc ấn tượng và công nghệ tự lái tiên tiến.',
 'ElectricCar', 1, 75.0, NOW(), 1, 4,
 'https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800',
 '["https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=400"]',
 '["Autopilot", "Camera 360", "Sạc siêu nhanh", "Màn hình cảm ứng 15 inch", "Âm thanh premium"]'),

(UUID(), 'Hyundai Kona Electric', 2023, '51C-22222', 5, 8000,
 '{"hourly": 70000, "daily": 700000, "monthly": 18000000}',
 'Rented',
 'SUV điện nhỏ gọn, tiết kiệm, phù hợp cho di chuyển trong thành phố.',
 'ElectricCar', 1, 64.0, NOW(), 2, 4,
 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800',
 '["https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400"]',
 '["GPS", "Camera lùi", "Sạc nhanh", "Hệ thống an toàn SmartSense"]'),

(UUID(), 'Nissan Leaf', 2022, '29D-33333', 5, 20000,
 '{"hourly": 60000, "daily": 600000, "monthly": 15000000}',
 'Available',
 'Hatchback điện phổ biến nhất thế giới, đáng tin cậy và tiết kiệm.',
 'ElectricCar', 1, 62.0, NOW(), 3, 4,
 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800',
 '["https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400"]',
 '["GPS", "Camera lùi", "Sạc nhanh", "Hệ thống ProPILOT"]'),

(UUID(), 'BYD Atto 3', 2023, '43E-44444', 5, 2000,
 '{"hourly": 65000, "daily": 650000, "monthly": 16000000}',
 'Maintenance',
 'SUV điện Trung Quốc với thiết kế trẻ trung và giá cả hợp lý.',
 'ElectricCar', 1, 60.5, NOW(), 2, 4,
 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=800',
 '["https://images.unsplash.com/photo-1617788138017-80ad40651399?w=400"]',
 '["GPS", "Camera 360", "Sạc nhanh", "Màn hình xoay"]'),

-- Electric Motorcycles
(UUID(), 'VinFast Feliz S', 2023, '43F1-12345', 2, 1000,
 '{"hourly": 20000, "daily": 150000, "monthly": 3000000}',
 'Available',
 'Xe máy điện phổ thông, phù hợp cho di chuyển hàng ngày trong thành phố.',
 'ElectricMotorcycle', 1, 2.5, NOW(), 4, 4,
 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800',
 '["https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=400"]',
 '["Khóa thông minh", "Cốp xe rộng", "Đèn LED", "Sạc nhanh"]'),

(UUID(), 'VinFast Klara S', 2023, '43F2-67890', 2, 500,
 '{"hourly": 25000, "daily": 180000, "monthly": 3500000}',
 'Available',
 'Xe máy điện cao cấp với thiết kế sang trọng và pin lớn.',
 'ElectricMotorcycle', 1, 3.2, NOW(), 4, 4,
 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800',
 '["https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=400"]',
 '["Khóa thông minh", "Màn hình LCD", "Đèn LED", "Sạc nhanh", "Phanh ABS"]'),

(UUID(), 'Yadea G5', 2023, '30F3-11111', 2, 800,
 '{"hourly": 18000, "daily": 130000, "monthly": 2500000}',
 'Available',
 'Xe máy điện giá rẻ, phù hợp cho sinh viên và người lao động.',
 'ElectricMotorcycle', 1, 2.0, NOW(), 4, 4,
 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800',
 '["https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=400"]',
 '["Khóa từ", "Đèn LED", "Cốp xe"]'),

(UUID(), 'Pega NewTech', 2023, '51F4-22222', 2, 1200,
 '{"hourly": 22000, "daily": 160000, "monthly": 3200000}',
 'Available',
 'Xe máy điện thể thao với thiết kế năng động và hiệu suất cao.',
 'ElectricMotorcycle', 1, 2.8, NOW(), 5, 4,
 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800',
 '["https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=400"]',
 '["Khóa thông minh", "Màn hình LCD", "Đèn LED", "Sạc nhanh", "Chế độ Sport"]');

-- Note: Replace UUID() with actual UUIDs in production or use a UUID generation function
-- The above uses MySQL's UUID() function which generates unique identifiers
