package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Vehicle;

@Service
public class VehicleService {
    private List<Vehicle> vehicles = new ArrayList<>();

    public VehicleService() {
        // Mock data từ demo JS
        vehicles.add(createVehicle("tesla-model-3", "Tesla Model 3", "ElectricCar", "Tesla", new BigDecimal("89"), true, 5));
        vehicles.add(createVehicle("vinfast-klara", "VinFast Klara", "ElectricMotorcycle", "VinFast", new BigDecimal("25"), false, 2));
        vehicles.add(createVehicle("honda-cbr", "Honda CBR 150R", "Motorcycle", "Honda", new BigDecimal("35"), true, 2));
        vehicles.add(createVehicle("yamaha-nmax", "Yamaha NMAX", "Motorcycle", "Yamaha", new BigDecimal("30"), true, 2));
        vehicles.add(createVehicle("tesla-model-y", "Tesla Model Y", "ElectricCar", "Tesla", new BigDecimal("95"), true, 7));
        vehicles.add(createVehicle("gogoro-viva", "Gogoro Viva", "ElectricMotorcycle", "Gogoro", new BigDecimal("20"), false, 2));
    }
    
    private Vehicle createVehicle(String id, String model, String type, String brand, BigDecimal pricePerDay, boolean requiresLicense, int seats) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(id);
        vehicle.setVehicleModel(model);
        vehicle.setVehicleType(type);
        vehicle.setPricePerDay(pricePerDay);
        vehicle.setPricePerHour(pricePerDay.divide(new BigDecimal("24"), 2, BigDecimal.ROUND_HALF_UP));
        vehicle.setPricePerMonth(pricePerDay.multiply(new BigDecimal("30")));
        vehicle.setRequiresLicense(requiresLicense);
        vehicle.setSeats(seats);
        vehicle.setStatus("Available");
        vehicle.setOdometer(0);
        vehicle.setDescription("High-quality " + type.toLowerCase() + " from " + brand);
        return vehicle;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicles;
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicles.stream().filter(v -> v.getVehicleId().equals(id)).findFirst();
    }
}