-- Insert sample data for Roles table
USE ecodana;

-- Insert roles if they don't exist
INSERT IGNORE INTO Roles (RoleId, RoleName, NormalizedName) VALUES 
('customer-role-id', 'Customer', 'CUSTOMER'),
('staff-role-id', 'Staff', 'STAFF'),
('admin-role-id', 'Admin', 'ADMIN');

-- Insert sample car brands
INSERT IGNORE INTO CarBrand (BrandId, BrandName) VALUES 
('brand-1', 'Toyota'),
('brand-2', 'Honda'),
('brand-3', 'Ford'),
('brand-4', 'VinFast'),
('brand-5', 'Tesla');

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
('cat-5', 'Electric Motorcycle', 'ElectricMotorcycle');

-- Insert sample car features
INSERT IGNORE INTO CarFeature (FeatureId, FeatureName) VALUES 
('feat-1', 'Air Conditioning'),
('feat-2', 'GPS Navigation'),
('feat-3', 'Bluetooth'),
('feat-4', 'USB Charging'),
('feat-5', 'Backup Camera'),
('feat-6', 'Keyless Entry'),
('feat-7', 'Cruise Control'),
('feat-8', 'Lane Assist');

-- Insert sample vehicles
INSERT IGNORE INTO Vehicle (VehicleId, BrandId, VehicleModel, YearManufactured, TransmissionTypeId, FuelTypeId, LicensePlate, Seats, Odometer, PricePerHour, PricePerDay, PricePerMonth, Status, Description, CategoryId, VehicleType, RequiresLicense, BatteryCapacity) VALUES 
('vehicle-1', 'brand-1', 'Camry Hybrid', 2023, 'trans-2', 'fuel-3', '30A-12345', 5, 15000, 15.00, 89.00, 2500.00, 'Available', 'Comfortable hybrid sedan with excellent fuel economy', 'cat-1', 'ElectricCar', 1, 65.0),
('vehicle-2', 'brand-4', 'VF e34', 2023, 'trans-2', 'fuel-2', '30B-67890', 5, 12000, 18.00, 95.00, 2800.00, 'Available', 'Vietnamese electric sedan with modern features', 'cat-1', 'ElectricCar', 1, 70.0),
('vehicle-3', 'brand-2', 'Wave Alpha', 2023, 'trans-1', 'fuel-1', '30C-11111', 2, 8000, 8.00, 45.00, 1200.00, 'Available', 'Reliable motorcycle for city commuting', 'cat-4', 'Motorcycle', 1, NULL),
('vehicle-4', 'brand-5', 'Model 3', 2023, 'trans-2', 'fuel-2', '30D-22222', 5, 20000, 25.00, 120.00, 3500.00, 'Available', 'Premium electric sedan with autopilot', 'cat-1', 'ElectricCar', 1, 75.0);

-- Insert sample discount
INSERT IGNORE INTO Discount (DiscountId, DiscountName, Description, DiscountType, DiscountValue, StartDate, EndDate, IsActive, CreatedDate, VoucherCode, MinOrderAmount, MaxDiscountAmount, UsageLimit, UsedCount, DiscountCategory) VALUES 
('discount-1', 'New User 10% Off', '10% discount for new users', 'Percentage', 10.00, '2024-01-01', '2024-12-31', 1, '2024-01-01 00:00:00', 'NEWUSER10', 50.00, 100.00, 1000, 0, 'General'),
('discount-2', 'Weekend Special', '15% off on weekends', 'Percentage', 15.00, '2024-01-01', '2024-12-31', 1, '2024-01-01 00:00:00', 'WEEKEND15', 100.00, 200.00, 500, 0, 'General');

-- Insert sample insurance
INSERT IGNORE INTO Insurance (InsuranceId, InsuranceName, InsuranceType, BaseRatePerDay, PercentageRate, CoverageAmount, ApplicableVehicleSeats, Description, IsActive, CreatedDate) VALUES 
('insurance-1', 'Basic Coverage', 'Basic', 5.00, 2.50, 50000000.00, '1-5', 'Basic insurance coverage for small vehicles', 1, '2024-01-01 00:00:00'),
('insurance-2', 'Premium Coverage', 'Premium', 10.00, 5.00, 100000000.00, '1-7', 'Premium insurance coverage for all vehicles', 1, '2024-01-01 00:00:00');

-- Insert sample terms
INSERT IGNORE INTO Terms (TermsId, Version, Title, ShortContent, FullContent, EffectiveDate, IsActive, CreatedDate) VALUES 
('terms-1', 'v1.0', 'Terms and Conditions v1.0', 'Basic terms and conditions for vehicle rental', 'Full terms and conditions content...', '2024-01-01', 1, '2024-01-01 00:00:00');

COMMIT;
