package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    private List<Vehicle> vehicles = new ArrayList<>();

    public VehicleService() {
        // Mock data từ demo JS
        vehicles.add(new Vehicle("tesla-model-3", "Tesla Model 3", "Electric Car", "Tesla", 89, true, "fas fa-car", "from-primary to-secondary"));
        vehicles.add(new Vehicle("vinfast-klara", "VinFast Klara", "Electric Motorcycle", "VinFast", 25, false, "fas fa-motorcycle", "from-secondary to-primary"));
        vehicles.add(new Vehicle("honda-cbr", "Honda CBR 150R", "Gasoline Motorcycle", "Honda", 35, true, "fas fa-motorcycle", "from-accent to-primary"));
        vehicles.add(new Vehicle("yamaha-nmax", "Yamaha NMAX", "Gasoline Motorcycle", "Yamaha", 30, true, "fas fa-motorcycle", "from-primary to-accent"));
        vehicles.add(new Vehicle("tesla-model-y", "Tesla Model Y", "Electric Car", "Tesla", 95, true, "fas fa-car", "from-secondary to-accent"));
        vehicles.add(new Vehicle("gogoro-viva", "Gogoro Viva", "Electric Motorcycle", "Gogoro", 20, false, "fas fa-motorcycle", "from-accent to-secondary"));
    }

    public List<Vehicle> getAllVehicles() {
        return vehicles;
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicles.stream().filter(v -> v.getId().equals(id)).findFirst();
    }
}