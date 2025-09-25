-- Insert test data for EvoDana application
USE ecodanav2;

-- Insert roles if they don't exist
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('customer-role-id', 'Customer', 'CUSTOMER'),
('staff-role-id', 'Staff', 'STAFF'),
('owner-role-id', 'Owner', 'OWNER'),
('admin-role-id', 'Admin', 'ADMIN');

-- Insert test users
-- Admin user
INSERT IGNORE INTO Users (
    UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName,
    RoleId, Status, CreatedDate, NormalizedUserName, NormalizedEmail,
    EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount,
    SecurityStamp, ConcurrencyStamp
) VALUES (
    'test-admin-id',
    'admin123',
    'admin123@test.com',
    '$2a$10$UT9rB0H9dIUZGdBjCvv9neADT48Bl6XUpNqpk4PA7QcqqLxIgqbQ6', -- admin123
    '0555555555',
    'Admin',
    'Test',
    'admin-role-id',
    'Active',
    NOW(),
    'ADMIN123',
    'ADMIN123@TEST.COM',
    1,
    0,
    0,
    0,
    UUID(),
    UUID()
);

-- Owner/Staff user
INSERT IGNORE INTO Users (
    UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName,
    RoleId, Status, CreatedDate, NormalizedUserName, NormalizedEmail,
    EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount,
    SecurityStamp, ConcurrencyStamp
) VALUES (
    'test-owner-id',
    'owner123',
    'owner123@test.com',
    '$2a$10$udCgxkgXEJkPLNts47c.vevinawbk2CTSbzhydw3mRP53Eq7mSiri', -- owner123
    '0987654321',
    'Owner',
    'Test',
    'owner-role-id',
    'Active',
    NOW(),
    'OWNER123',
    'OWNER123@TEST.COM',
    1,
    0,
    0,
    0,
    UUID(),
    UUID()
);

-- Customer user
INSERT IGNORE INTO Users (
    UserId, Username, Email, PasswordHash, PhoneNumber, FirstName, LastName,
    RoleId, Status, CreatedDate, NormalizedUserName, NormalizedEmail,
    EmailVerifed, TwoFactorEnabled, LockoutEnabled, AccessFailedCount,
    SecurityStamp, ConcurrencyStamp
) VALUES (
    'test-customer-id',
    'testcustomer',
    'customer@test.com',
    '$2a$10$iBRgVDf1WKOpICJ.V2IUGuuOKu54hhNg5b8rcAlvO/6ND8hxia56a', -- admin123
    '0123456789',
    'Test',
    'Customer',
    'customer-role-id',
    'Active',
    NOW(),
    'TESTCUSTOMER',
    'CUSTOMER@TEST.COM',
    1,
    0,
    0,
    0,
    UUID(),
    UUID()
);

-- Insert sample car brands
INSERT IGNORE INTO CarBrand (BrandId, BrandName) VALUES 
('brand-1', 'Toyota'),
('brand-2', 'Honda'),
('brand-3', 'Ford'),
('brand-4', 'VinFast'),
('brand-5', 'Tesla'),
('brand-6', 'BMW'),
('brand-7', 'Mercedes'),
('brand-8', 'Audi');

-- Insert sample fuel types
INSERT IGNORE INTO FuelType (FuelTypeId, FuelName) VALUES 
('fuel-1', 'Gasoline'),
('fuel-2', 'Electric'),
('fuel-3', 'Hybrid'),
('fuel-4', 'Diesel');

-- Insert sample transmission types
INSERT IGNORE INTO TransmissionType (TransmissionTypeId, TransmissionName) VALUES 
('trans-1', 'Manual'),
('trans-2', 'Automatic'),
('trans-3', 'CVT'),
('trans-4', 'Semi-Automatic');

-- Insert sample vehicle categories
INSERT IGNORE INTO VehicleCategories (CategoryId, CategoryName, VehicleType) VALUES 
('cat-1', 'Sedan', 'ElectricCar'),
('cat-2', 'SUV', 'ElectricCar'),
('cat-3', 'Hatchback', 'ElectricCar'),
('cat-4', 'Standard Motorcycle', 'Motorcycle'),
('cat-5', 'Electric Motorcycle', 'ElectricMotorcycle'),
('cat-6', 'Luxury Car', 'ElectricCar'),
('cat-7', 'Sports Car', 'ElectricCar');

-- Insert sample car features
INSERT IGNORE INTO CarFeature (FeatureId, FeatureName) VALUES 
('feat-1', 'Air Conditioning'),
('feat-2', 'GPS Navigation'),
('feat-3', 'Bluetooth'),
('feat-4', 'USB Charging'),
('feat-5', 'Backup Camera'),
('feat-6', 'Keyless Entry'),
('feat-7', 'Cruise Control'),
('feat-8', 'Lane Assist'),
('feat-9', 'Sunroof'),
('feat-10', 'Leather Seats');

-- Insert sample vehicles
INSERT IGNORE INTO Vehicle (VehicleId, BrandId, VehicleModel, YearManufactured, TransmissionTypeId, FuelTypeId, LicensePlate, Seats, Odometer, PricePerHour, PricePerDay, PricePerMonth, Status, Description, CategoryId, VehicleType, RequiresLicense, BatteryCapacity) VALUES 
('vehicle-1', 'brand-1', 'Camry Hybrid', 2023, 'trans-2', 'fuel-3', '30A-12345', 5, 15000, 15.00, 89.00, 2500.00, 'Available', 'Comfortable hybrid sedan with excellent fuel economy', 'cat-1', 'ElectricCar', 1, 65.0),
('vehicle-2', 'brand-4', 'VF e34', 2023, 'trans-2', 'fuel-2', '30B-67890', 5, 12000, 18.00, 95.00, 2800.00, 'Available', 'Vietnamese electric sedan with modern features', 'cat-1', 'ElectricCar', 1, 70.0),
('vehicle-3', 'brand-2', 'Wave Alpha', 2023, 'trans-1', 'fuel-1', '30C-11111', 2, 8000, 8.00, 45.00, 1200.00, 'Available', 'Reliable motorcycle for city commuting', 'cat-4', 'Motorcycle', 1, NULL),
('vehicle-4', 'brand-5', 'Model 3', 2023, 'trans-2', 'fuel-2', '30D-22222', 5, 20000, 25.00, 120.00, 3500.00, 'Available', 'Premium electric sedan with autopilot', 'cat-1', 'ElectricCar', 1, 75.0),
('vehicle-5', 'brand-6', 'X5', 2023, 'trans-2', 'fuel-1', '30E-33333', 7, 18000, 35.00, 180.00, 4500.00, 'Available', 'Luxury SUV with premium features', 'cat-2', 'ElectricCar', 1, NULL),
('vehicle-6', 'brand-7', 'C-Class', 2023, 'trans-2', 'fuel-1', '30F-44444', 5, 16000, 30.00, 150.00, 3800.00, 'Booked', 'Elegant sedan with advanced technology', 'cat-1', 'ElectricCar', 1, NULL);

-- Insert sample discount
INSERT IGNORE INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, CreatedDate, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory) VALUES 
('discount-1', 'New User 10% Off', '10% discount for new users', 'Percentage', 10.00, '2024-01-01', '2024-12-31', 1, '2024-01-01 00:00:00', 'NEWUSER10', 50.00, 100.00, 1000, 0, 'General'),
('discount-2', 'Weekend Special', '15% off on weekends', 'Percentage', 15.00, '2024-01-01', '2024-12-31', 1, '2024-01-01 00:00:00', 'WEEKEND15', 100.00, 200.00, 500, 0, 'General'),
('discount-3', 'First Time User', '20% off for first booking', 'Percentage', 20.00, '2024-01-01', '2024-12-31', 1, '2024-01-01 00:00:00', 'FIRST20', 30.00, 50.00, 200, 0, 'General');

-- Insert sample insurance
INSERT IGNORE INTO Insurance (InsuranceId, InsuranceName, InsuranceType, BaseRatePerDay, PercentageRate, CoverageAmount, ApplicableVehicleSeats, Description, IsActive, CreatedDate) VALUES 
('insurance-1', 'Basic Coverage', 'Basic', 5.00, 2.50, 50000000.00, '1-5', 'Basic insurance coverage for small vehicles', 1, '2024-01-01 00:00:00'),
('insurance-2', 'Premium Coverage', 'Premium', 10.00, 5.00, 100000000.00, '1-7', 'Premium insurance coverage for all vehicles', 1, '2024-01-01 00:00:00'),
('insurance-3', 'Luxury Coverage', 'Luxury', 15.00, 7.50, 200000000.00, '1-7', 'Luxury insurance coverage for high-end vehicles', 1, '2024-01-01 00:00:00');

-- Insert sample terms
INSERT IGNORE INTO Terms (TermsId, Version, Title, ShortContent, FullContent, EffectiveDate, IsActive, CreatedDate) VALUES 
('terms-1', 'v1.0', 'Terms and Conditions v1.0', 'Basic terms and conditions for vehicle rental', 'Full terms and conditions content...', '2024-01-01', 1, '2024-01-01 00:00:00');

-- Insert sample bookings
INSERT IGNORE INTO Booking (BookingId, UserId, VehicleId, StartDate, EndDate, TotalAmount, Status, CreatedDate, UpdatedDate, PaymentStatus, InsuranceId, DiscountId) VALUES 
('booking-1', 'test-customer-id', 'vehicle-1', '2024-02-15 08:00:00', '2024-02-18 18:00:00', 267.00, 'Active', NOW(), NOW(), 'Paid', 'insurance-1', 'discount-1'),
('booking-2', 'test-customer-id', 'vehicle-4', '2024-02-20 09:00:00', '2024-02-22 17:00:00', 240.00, 'Pending', NOW(), NOW(), 'Pending', 'insurance-2', NULL),
('booking-3', 'test-customer-id', 'vehicle-5', '2024-02-25 10:00:00', '2024-02-28 16:00:00', 540.00, 'Completed', '2024-02-20 10:00:00', '2024-02-28 16:00:00', 'Paid', 'insurance-2', 'discount-2');

COMMIT;
