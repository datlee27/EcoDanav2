-- Sample data for reference tables
-- This file contains sample data for CarBrand, FuelType, TransmissionType, and VehicleCategories

-- Insert Car Brands
INSERT INTO CarBrand (BrandId, BrandName) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Toyota'),
('550e8400-e29b-41d4-a716-446655440011', 'Honda');

-- Insert Fuel Types
INSERT INTO FuelType (FuelTypeId, FuelName) VALUES
('550e8400-e29b-41d4-a716-446655440030', 'Petrol'),
('550e8400-e29b-41d4-a716-446655440031', 'Diesel');

-- Insert Transmission Types
INSERT INTO TransmissionType (TransmissionTypeId, TransmissionName) VALUES
('550e8400-e29b-41d4-a716-446655440040', 'Manual'),
('550e8400-e29b-41d4-a716-446655440041', 'Automatic');

-- Insert Vehicle Categories
INSERT INTO VehicleCategories (CategoryId, CategoryName, VehicleType) VALUES
('550e8400-e29b-41d4-a716-446655440050', 'Economy', 'Car'),
('550e8400-e29b-41d4-a716-446655440051', 'Compact', 'Car'),
('550e8400-e29b-41d4-a716-446655440052', 'Mid-size', 'Car');
-- Insert Car Features (optional reference data)
INSERT INTO CarFeature (FeatureId, FeatureName) VALUES
('550e8400-e29b-41d4-a716-446655440070', 'Air Conditioning'),
('550e8400-e29b-41d4-a716-446655440071', 'GPS Navigation');