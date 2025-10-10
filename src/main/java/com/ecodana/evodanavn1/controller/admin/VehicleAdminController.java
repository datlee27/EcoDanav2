package com.ecodana.evodanavn1.controller.admin;

import com.ecodana.evodanavn1.dto.VehicleRequest;
import com.ecodana.evodanavn1.dto.VehicleResponse;
import com.ecodana.evodanavn1.model.TransmissionType;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.VehicleCategories;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/vehicles")
public class VehicleAdminController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    /**
     * Display vehicle management page
     */
    @GetMapping
    public String vehicleManagementPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }

        List<VehicleResponse> vehicles = vehicleService.getAllVehicleResponses();
        List<VehicleCategories> categories = vehicleService.getAllCategories();
        List<TransmissionType> transmissionTypes = vehicleService.getAllTransmissionTypes();

        model.addAttribute("currentUser", user);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("categories", categories);
        model.addAttribute("transmissionTypes", transmissionTypes);
        model.addAttribute("totalVehicles", vehicles.size());

        return "admin/admin-vehicles-management";
    }

    /**
     * Display add vehicle page
     */
    @GetMapping("/add")
    public String addVehiclePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }

        List<VehicleCategories> categories = vehicleService.getAllCategories();
        List<TransmissionType> transmissionTypes = vehicleService.getAllTransmissionTypes();

        model.addAttribute("currentUser", user);
        model.addAttribute("categories", categories);
        model.addAttribute("transmissionTypes", transmissionTypes);

        return "admin/vehicle-add";
    }

    /**
     * Display edit vehicle page
     */
    @GetMapping("/edit/{id}")
    public String editVehiclePage(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }

        try {
            VehicleResponse vehicle = vehicleService.getVehicleResponseById(id);
            List<VehicleCategories> categories = vehicleService.getAllCategories();
            List<TransmissionType> transmissionTypes = vehicleService.getAllTransmissionTypes();

            model.addAttribute("currentUser", user);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("categories", categories);
            model.addAttribute("transmissionTypes", transmissionTypes);

            return "admin/vehicle-edit";
        } catch (Exception e) {
            return "redirect:/admin/vehicles?error=Vehicle not found";
        }
    }

    /**
     * Display vehicle detail page
     */
    @GetMapping("/detail/{id}")
    public String vehicleDetailPage(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }

        try {
            VehicleResponse vehicle = vehicleService.getVehicleResponseById(id);
            model.addAttribute("currentUser", user);
            model.addAttribute("vehicle", vehicle);

            return "admin/vehicle-detail";
        } catch (Exception e) {
            return "redirect:/admin/vehicles?error=Vehicle not found";
        }
    }

    /**
     * API: Get all vehicles
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllVehicles(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<VehicleResponse> vehicles = vehicleService.getAllVehicleResponses();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vehicles", vehicles);
            response.put("total", vehicles.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error fetching vehicles: " + e.getMessage()));
        }
    }

    /**
     * API: Get vehicle by ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVehicleById(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            VehicleResponse vehicle = vehicleService.getVehicleResponseById(id);
            return ResponseEntity.ok(Map.of("success", true, "vehicle", vehicle));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Vehicle not found"));
        }
    }

    /**
     * API: Create new vehicle
     */
    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createVehicle(@RequestBody VehicleRequest request, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            // Validate license plate uniqueness
            if (vehicleService.vehicleExistsByLicensePlate(request.getLicensePlate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "License plate already exists"));
            }

            VehicleResponse vehicle = vehicleService.createVehicle(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Vehicle created successfully", "vehicle", vehicle));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error creating vehicle: " + e.getMessage()));
        }
    }

    /**
     * API: Update vehicle
     */
    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable String id, @RequestBody VehicleRequest request, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            // Validate license plate uniqueness (excluding current vehicle)
            if (vehicleService.vehicleExistsByLicensePlateAndNotId(request.getLicensePlate(), id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "License plate already exists"));
            }

            VehicleResponse vehicle = vehicleService.updateVehicleById(id, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Vehicle updated successfully", "vehicle", vehicle));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating vehicle: " + e.getMessage()));
        }
    }

    /**
     * API: Delete vehicle
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Vehicle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error deleting vehicle: " + e.getMessage()));
        }
    }

    /**
     * API: Update vehicle status
     */
    @PatchMapping("/api/status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVehicleStatus(@PathVariable String id, @RequestParam String status, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            vehicleService.updateVehicleStatus(id, status);
            return ResponseEntity.ok(Map.of("success", true, "message", "Vehicle status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating vehicle status: " + e.getMessage()));
        }
    }

    /**
     * API: Search vehicles
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchVehicles(@RequestParam String keyword, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<VehicleResponse> vehicles = vehicleService.searchVehicles(keyword).stream()
                    .map(VehicleResponse::new)
                    .toList();
            return ResponseEntity.ok(Map.of("success", true, "vehicles", vehicles, "total", vehicles.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error searching vehicles: " + e.getMessage()));
        }
    }

    /**
     * API: Get categories
     */
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategories(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<VehicleCategories> categories = vehicleService.getAllCategories();
            return ResponseEntity.ok(Map.of("success", true, "categories", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error fetching categories: " + e.getMessage()));
        }
    }

    /**
     * API: Get transmission types
     */
    @GetMapping("/api/transmission-types")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTransmissionTypes(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            List<TransmissionType> transmissionTypes = vehicleService.getAllTransmissionTypes();
            return ResponseEntity.ok(Map.of("success", true, "transmissionTypes", transmissionTypes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error fetching transmission types: " + e.getMessage()));
        }
    }
}
