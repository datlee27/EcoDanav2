package com.ecodana.evodanavn1.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ecodana.evodanavn1.model.Role;
import com.ecodana.evodanavn1.model.TransmissionType;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.model.VehicleCategories;
import com.ecodana.evodanavn1.repository.RoleRepository;
import com.ecodana.evodanavn1.repository.TransmissionTypeRepository;
import com.ecodana.evodanavn1.repository.VehicleCategoriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.service.AnalyticsService;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.RoleService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VehicleCategoriesRepository vehicleCategoriesRepository;

    @Autowired
    private TransmissionTypeRepository transmissionTypeRepository;

    @GetMapping({"/admin", "/admin/dashboard"})
    public String adminDashboard(@RequestParam(required = false) String tab, HttpSession session, Model model, HttpServletResponse response) {
        response.setHeader("Connection", "close");
        response.setHeader("Content-Encoding", "identity");
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            user = userService.getUserWithRole("admin@ecodana.com");
            if (user == null) {
                return "redirect:/login";
            }
            session.setAttribute("currentUser", user);
        }

        User userWithRole = userService.getUserWithRole(user.getEmail());
        if (userWithRole == null || !userService.isAdmin(userWithRole)) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", userWithRole);

        try {
            model.addAttribute("analytics", analyticsService.getDashboardAnalytics());
            model.addAttribute("realTimeData", analyticsService.getRealTimeData());
            model.addAttribute("performanceMetrics", analyticsService.getPerformanceMetrics());
            model.addAttribute("systemHealth", analyticsService.getSystemHealth());

            List<User> allUsers = userService.getAllUsersWithRole();
            model.addAttribute("users", allUsers.size() > 100 ? allUsers.subList(0, 100) : allUsers);

            List<Vehicle> allVehicles = vehicleService.getAllVehicles();
            model.addAttribute("vehicles", allVehicles.size() > 100 ? allVehicles.subList(0, 100) : allVehicles);

            List<?> allBookings = bookingService.getAllBookings();
            model.addAttribute("bookings", allBookings.size() > 100 ? allBookings.subList(0, 100) : allBookings);

            model.addAttribute("discounts", List.of());
            model.addAttribute("insuranceList", List.of());
            model.addAttribute("contracts", List.of());
            model.addAttribute("payments", List.of());
            model.addAttribute("notifications", List.of());

            model.addAttribute("user", userWithRole);
            model.addAttribute("tab", tab != null ? tab : "overview");
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("totalBookings", allBookings.size());
            model.addAttribute("totalUsers", allUsers.size());

            return "admin/admin-dashboard";
        } catch (Exception e) {
            logger.error("Error loading admin data: " + e.getMessage(), e);
            // Fallback with empty data
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);
            model.addAttribute("vehicles", List.of());
            model.addAttribute("bookings", List.of());
            model.addAttribute("users", List.of());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("tab", tab != null ? tab : "overview");
            model.addAttribute("analytics", new HashMap<>());
            return "admin/admin-dashboard";
        }
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) return "redirect:/login";

        List<?> allBookings = bookingService.getAllBookings();
        model.addAttribute("bookings", allBookings);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", bookingService.getPendingBookings().size());
        model.addAttribute("confirmedBookings", bookingService.getActiveBookings().size());
        model.addAttribute("cancelledBookings", 0);
        return "admin/bookings-management";
    }

    @GetMapping("/admin/vehicles")
    public String adminVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) return "redirect:/login";

        List<Vehicle> allVehicles = vehicleService.getAllVehicles();
        model.addAttribute("vehicles", allVehicles);
        model.addAttribute("totalVehicles", allVehicles.size());
        model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles().size());
        model.addAttribute("inUseVehicles", allVehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.Rented).count());
        model.addAttribute("maintenanceVehicles", allVehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.Maintenance).count());
        return "admin/vehicles-management";
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) return "redirect:/login";

        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("activeUsers", allUsers.stream().filter(u -> u.getStatus() == User.UserStatus.Active).count());
        model.addAttribute("pendingUsers", 0);
        model.addAttribute("suspendedUsers", allUsers.stream().filter(u -> u.getStatus() == User.UserStatus.Banned).count());
        return "admin/users-management";
    }

    @GetMapping("/admin/reports")
    public String adminReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) return "redirect:/login";

        model.addAttribute("totalRevenue", bookingService.getTotalRevenue());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());
        model.addAttribute("activeUsers", userService.getAllUsers().size());
        return "admin/reports";
    }

    @GetMapping("/admin/api/analytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        try {
            return ResponseEntity.ok(analyticsService.getDashboardAnalytics());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // User CRUD Operations
    @PostMapping("/admin/users/add")
    public String addUser(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }

        try {
            if (params.values().stream().anyMatch(v -> v == null || v.trim().isEmpty())) {
                redirectAttributes.addFlashAttribute("error", "All required fields must be filled");
                return "redirect:/admin/dashboard?tab=users";
            }
            if (userService.existsByUsername(params.get("username"))) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/admin/dashboard?tab=users";
            }
            if (userService.existsByEmail(params.get("email"))) {
                redirectAttributes.addFlashAttribute("error", "Email already exists");
                return "redirect:/admin/dashboard?tab=users";
            }

            User newUser = new User();
            newUser.setId(java.util.UUID.randomUUID().toString());
            newUser.setFirstName(params.get("firstName"));
            newUser.setLastName(params.get("lastName"));
            newUser.setUsername(params.get("username"));
            newUser.setEmail(params.get("email"));
            newUser.setPhoneNumber(params.get("phoneNumber"));
            newUser.setPassword(passwordEncoder.encode(params.get("password")));
            newUser.setStatus(User.UserStatus.valueOf(params.get("status")));
            newUser.setCreatedDate(LocalDateTime.now());

            roleRepository.findByRoleName(params.get("role")).ifPresent(role -> {
                newUser.setRoleId(role.getRoleId());
                newUser.setRole(role);
            });

            userService.save(newUser);
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
        } catch (Exception e) {
            logger.error("Error adding user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error adding user: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    @GetMapping("/admin/users/edit/{id}")
    public String editUserForm(@PathVariable String id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            return "redirect:/login";
        }

        try {
            User user = userService.findById(id);
            if (user != null) {
                model.addAttribute("editUser", user);
                model.addAttribute("roles", roleRepository.findAll()); // Pass roles for dropdown
                model.addAttribute("tab", "users");
                return "admin/edit-user";
            }
        } catch (Exception e) {
            logger.error("Error loading user for edit: " + e.getMessage(), e);
        }
        redirectAttributes.addFlashAttribute("error", "User not found or error loading.");
        return "redirect:/admin/dashboard?tab=users";
    }

    @PostMapping("/admin/users/update")
    public String updateUser(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }

        try {
            User user = userService.findById(params.get("userId"));
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/admin/dashboard?tab=users";
            }

            user.setFirstName(params.get("firstName"));
            user.setLastName(params.get("lastName"));
            user.setUsername(params.get("username"));
            user.setEmail(params.get("email"));
            user.setPhoneNumber(params.get("phoneNumber"));
            user.setStatus(User.UserStatus.valueOf(params.get("status")));

            if (params.get("password") != null && !params.get("password").trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(params.get("password")));
            }

            roleRepository.findByRoleName(params.get("role")).ifPresent(role -> user.setRoleId(role.getRoleId()));

            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }

        try {
            if (userService.findById(id) != null) {
                userService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    @PostMapping("/admin/api/users/add")
    public ResponseEntity<Map<String, Object>> addUserApi(@RequestParam Map<String, String> params, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
        }

        try {
            if (params.values().stream().anyMatch(v -> v == null || v.trim().isEmpty())) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }
            if (userService.existsByUsername(params.get("username"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }
            if (userService.existsByEmail(params.get("email"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            User newUser = new User();
            newUser.setFirstName(params.get("firstName").trim());
            newUser.setLastName(params.get("lastName").trim());
            newUser.setUsername(params.get("username").trim());
            newUser.setEmail(params.get("email").trim());
            newUser.setPhoneNumber(params.get("phoneNumber").trim());
            newUser.setPassword(params.get("password")); // Registration will encode it

            roleService.findByRoleName(params.get("role")).ifPresent(role -> newUser.setRoleId(role.getRoleId()));

            newUser.setStatus(User.UserStatus.valueOf(params.get("status")));

            if (userService.register(newUser)) {
                User createdUser = userService.findByEmail(newUser.getEmail());
                return ResponseEntity.ok(Map.of("success", true, "message", "User created successfully", "userId", createdUser != null ? createdUser.getId() : "unknown"));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create user"));
            }
        } catch (Exception e) {
            logger.error("Error adding user via API: " + e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Vehicle CRUD Operations

    @GetMapping("/admin/vehicles/add")
    public String addVehicleForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            return "redirect:/login";
        }
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("categories", vehicleCategoriesRepository.findAll());
        model.addAttribute("transmissionTypes", transmissionTypeRepository.findAll());
        model.addAttribute("tab", "vehicles");
        return "admin/edit-vehicle"; // Reusing edit-vehicle for add form
    }

    @PostMapping("/admin/vehicles/add")
    public String addVehicle(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=vehicles";
        }

        try {
            Vehicle newVehicle = new Vehicle();
            newVehicle.setVehicleId(UUID.randomUUID().toString());
            newVehicle.setVehicleModel(params.get("vehicleModel"));
            newVehicle.setYearManufactured(Integer.parseInt(params.get("yearManufactured")));
            newVehicle.setLicensePlate(params.get("licensePlate"));
            newVehicle.setSeats(Integer.parseInt(params.get("seats")));
            newVehicle.setOdometer(Integer.parseInt(params.get("odometer")));
            newVehicle.setRentalPrices(params.get("rentalPrices")); // Expecting JSON string
            newVehicle.setStatus(Vehicle.VehicleStatus.valueOf(params.get("status")));
            newVehicle.setDescription(params.get("description"));
            newVehicle.setVehicleType(Vehicle.VehicleType.valueOf(params.get("vehicleType")));
            newVehicle.setRequiresLicense(Boolean.parseBoolean(params.get("requiresLicense")));
            newVehicle.setBatteryCapacity(new BigDecimal(params.get("batteryCapacity")));
            newVehicle.setMainImageUrl(params.get("mainImageUrl"));
            newVehicle.setImageUrls(params.get("imageUrls")); // Expecting JSON string
            newVehicle.setFeatures(params.get("features")); // Expecting JSON string
            newVehicle.setCreatedDate(LocalDateTime.now());
            newVehicle.setLastUpdatedBy(currentUser);

            // Set Category
            String categoryId = params.get("categoryId");
            if (categoryId != null && !categoryId.isEmpty()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(categoryId)).ifPresent(newVehicle::setCategory);
            }

            // Set Transmission Type
            String transmissionTypeId = params.get("transmissionTypeId");
            if (transmissionTypeId != null && !transmissionTypeId.isEmpty()) {
                transmissionTypeRepository.findById(Integer.parseInt(transmissionTypeId)).ifPresent(newVehicle::setTransmissionType);
            }

            vehicleService.saveVehicle(newVehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle added successfully!");
        } catch (Exception e) {
            logger.error("Error adding vehicle: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error adding vehicle: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=vehicles";
    }

    @GetMapping("/admin/vehicles/edit/{id}")
    public String editVehicleForm(@PathVariable String id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            return "redirect:/login";
        }

        try {
            Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);
            if (vehicleOptional.isPresent()) {
                model.addAttribute("vehicle", vehicleOptional.get());
                model.addAttribute("categories", vehicleCategoriesRepository.findAll());
                model.addAttribute("transmissionTypes", transmissionTypeRepository.findAll());
                model.addAttribute("tab", "vehicles");
                return "admin/edit-vehicle";
            }
        } catch (Exception e) {
            logger.error("Error loading vehicle for edit: " + e.getMessage(), e);
        }
        redirectAttributes.addFlashAttribute("error", "Vehicle not found or error loading.");
        return "redirect:/admin/dashboard?tab=vehicles";
    }

    @PostMapping("/admin/vehicles/update")
    public String updateVehicle(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=vehicles";
        }

        try {
            Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(params.get("vehicleId"));
            if (vehicleOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found");
                return "redirect:/admin/dashboard?tab=vehicles";
            }

            Vehicle existingVehicle = vehicleOptional.get();
            existingVehicle.setVehicleModel(params.get("vehicleModel"));
            existingVehicle.setYearManufactured(Integer.parseInt(params.get("yearManufactured")));
            existingVehicle.setLicensePlate(params.get("licensePlate"));
            existingVehicle.setSeats(Integer.parseInt(params.get("seats")));
            existingVehicle.setOdometer(Integer.parseInt(params.get("odometer")));
            existingVehicle.setRentalPrices(params.get("rentalPrices"));
            existingVehicle.setStatus(Vehicle.VehicleStatus.valueOf(params.get("status")));
            existingVehicle.setDescription(params.get("description"));
            existingVehicle.setVehicleType(Vehicle.VehicleType.valueOf(params.get("vehicleType")));
            existingVehicle.setRequiresLicense(Boolean.parseBoolean(params.get("requiresLicense")));
            existingVehicle.setBatteryCapacity(new BigDecimal(params.get("batteryCapacity")));
            existingVehicle.setMainImageUrl(params.get("mainImageUrl"));
            existingVehicle.setImageUrls(params.get("imageUrls"));
            existingVehicle.setFeatures(params.get("features"));
            existingVehicle.setLastUpdatedBy(currentUser);

            // Update Category
            String categoryId = params.get("categoryId");
            if (categoryId != null && !categoryId.isEmpty()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(categoryId)).ifPresent(existingVehicle::setCategory);
            }

            // Update Transmission Type
            String transmissionTypeId = params.get("transmissionTypeId");
            if (transmissionTypeId != null && !transmissionTypeId.isEmpty()) {
                transmissionTypeRepository.findById(Integer.parseInt(transmissionTypeId)).ifPresent(existingVehicle::setTransmissionType);
            }

            vehicleService.updateVehicle(existingVehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating vehicle: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating vehicle: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=vehicles";
    }

    @GetMapping("/admin/vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=vehicles";
        }

        try {
            if (vehicleService.getVehicleById(id).isPresent()) {
                vehicleService.deleteVehicle(id);
                redirectAttributes.addFlashAttribute("success", "Vehicle deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting vehicle: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting vehicle: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=vehicles";
    }

    @PostMapping("/admin/api/vehicles/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVehicleStatusApi(@RequestParam String vehicleId, @RequestParam String status, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
        }

        try {
            Vehicle updatedVehicle = vehicleService.updateVehicleStatus(vehicleId, status);
            if (updatedVehicle != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Vehicle status updated to " + status));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Vehicle not found or status update failed."));
            }
        } catch (Exception e) {
            logger.error("Error updating vehicle status via API: " + e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error updating vehicle status: " + e.getMessage()));
        }
    }
}