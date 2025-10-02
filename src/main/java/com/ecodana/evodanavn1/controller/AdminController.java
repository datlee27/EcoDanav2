package com.ecodana.evodanavn1.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.ecodana.evodanavn1.model.Role;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.RoleRepository;
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

    @GetMapping({"/admin", "/admin/dashboard"})
    public String adminDashboard(@RequestParam(required = false) String tab, HttpSession session, Model model, HttpServletResponse response) {
        System.out.println("Admin dashboard accessed");
        
        // Set response headers to prevent chunked encoding issues
        response.setHeader("Connection", "close");
        response.setHeader("Content-Encoding", "identity");
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            // For testing purposes, use admin user
            user = userService.getUserWithRole("admin@ecodana.com");
            if (user == null) {
                System.out.println("Admin user not found, redirecting to login");
                return "redirect:/login";
            }
            // Set user in session for future requests
            session.setAttribute("currentUser", user);
        }
        
        System.out.println("User found: " + user.getEmail());
        
        // Reload user with role information
        User userWithRole = userService.getUserWithRole(user.getEmail());
        if (userWithRole == null) {
            System.out.println("Could not load user with role, redirecting to login");
            return "redirect:/login";
        }
        
        System.out.println("User role: " + userWithRole.getRoleName());
        
        // Check if user has admin role
        if (!userService.isAdmin(userWithRole)) {
            System.out.println("User is not admin, redirecting to login");
            return "redirect:/login";
        }
        
        // Set current user in model for template
        model.addAttribute("currentUser", userWithRole);
        
        System.out.println("User is admin, loading dashboard data");
        
        try {
            // Load comprehensive analytics data
            System.out.println("Loading analytics data...");
            Map<String, Object> analytics = analyticsService.getDashboardAnalytics();
            Map<String, Object> realTimeData = analyticsService.getRealTimeData();
            Map<String, Object> performanceMetrics = analyticsService.getPerformanceMetrics();
            Map<String, Object> systemHealth = analyticsService.getSystemHealth();
            
            // Load individual data for specific tabs
            List<?> allVehicles = vehicleService.getAllVehicles();
            List<?> allBookings = bookingService.getAllBookings();
            List<?> allUsers = userService.getAllUsersWithRole();
            
            System.out.println("Loaded users with role info: " + allUsers.size());
            for (Object obj : allUsers) {
                User u = (User) obj;
                System.out.println("  User: " + u.getUsername() + " - " + u.getEmail() + " - " + u.getFirstName() + " " + u.getLastName() + " - Role: " + (u.getRole() != null ? u.getRole().getRoleName() : "No Role"));
            }
            
            // Limit data size to prevent chunked encoding issues
            if (allUsers.size() > 100) {
                allUsers = allUsers.subList(0, 100);
            }
            if (allVehicles.size() > 100) {
                allVehicles = allVehicles.subList(0, 100);
            }
            if (allBookings.size() > 100) {
                allBookings = allBookings.subList(0, 100);
            }
            
            // Mock data for new features - these services need to be implemented
            List<?> allDiscounts = List.of(); // discountService.getAllDiscounts();
            List<?> allInsurance = List.of(); // insuranceService.getAllInsurance();
            List<?> allContracts = List.of(); // bookingService.getAllContracts();
            List<?> allPayments = List.of(); // bookingService.getAllPayments();
            List<?> allNotifications = List.of(); // userService.getAllNotifications();
            
            // Add all analytics data to model
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);
            model.addAttribute("tab", tab != null ? tab : "overview");
            
            // Basic data
            model.addAttribute("vehicles", allVehicles);
            model.addAttribute("bookings", allBookings);
            model.addAttribute("users", allUsers);
            model.addAttribute("discounts", allDiscounts);
            model.addAttribute("insuranceList", allInsurance);
            model.addAttribute("contracts", allContracts);
            model.addAttribute("payments", allPayments);
            model.addAttribute("notifications", allNotifications);
            
            // Analytics data
            model.addAttribute("analytics", analytics);
            model.addAttribute("realTimeData", realTimeData);
            model.addAttribute("performanceMetrics", performanceMetrics);
            model.addAttribute("systemHealth", systemHealth);
            
            // Individual counts for backward compatibility - use actual data, not analytics
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("totalBookings", allBookings.size());
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("totalRevenue", analytics.get("totalRevenue"));
            model.addAttribute("activeSessions", analytics.get("activeSessions"));
            
            System.out.println("Returning admin/admin-dashboard template with analytics data");
            System.out.println("Data sizes - Users: " + allUsers.size() + ", Vehicles: " + allVehicles.size() + ", Bookings: " + allBookings.size());
            return "admin/admin-dashboard";
        } catch (Exception e) {
            System.out.println("Error loading admin data: " + e.getMessage());
            logger.error("Error occurred", e);
            
            // Fallback to mock data if database is not available
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);
            model.addAttribute("vehicles", List.of());
            model.addAttribute("bookings", List.of());
            model.addAttribute("users", List.of());
            model.addAttribute("discounts", List.of());
            model.addAttribute("insuranceList", List.of());
            model.addAttribute("contracts", List.of());
            model.addAttribute("payments", List.of());
            model.addAttribute("notifications", List.of());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("activeSessions", 1);
            model.addAttribute("tab", tab != null ? tab : "overview");
            
            // Mock analytics data
            model.addAttribute("analytics", new java.util.HashMap<>());
            model.addAttribute("realTimeData", new java.util.HashMap<>());
            model.addAttribute("performanceMetrics", new java.util.HashMap<>());
            model.addAttribute("systemHealth", new java.util.HashMap<>());
            
            System.out.println("Returning admin/admin-dashboard template with fallback data");
            return "admin/admin-dashboard";
        }
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        List<?> allBookings = bookingService.getAllBookings();
        List<?> pendingBookings = bookingService.getPendingBookings();
        List<?> confirmedBookings = bookingService.getActiveBookings();
        
        model.addAttribute("bookings", allBookings);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", pendingBookings.size());
        model.addAttribute("confirmedBookings", confirmedBookings.size());
        model.addAttribute("cancelledBookings", 0); // Mock data
        
        return "admin/bookings-management";
    }

    @GetMapping("/admin/vehicles")
    public String adminVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        List<?> allVehicles = vehicleService.getAllVehicles();
        List<?> availableVehicles = vehicleService.getAvailableVehicles();
        
        model.addAttribute("vehicles", allVehicles);
        model.addAttribute("totalVehicles", allVehicles.size());
        model.addAttribute("availableVehicles", availableVehicles.size());
        model.addAttribute("inUseVehicles", 0); // Mock data
        model.addAttribute("maintenanceVehicles", 0); // Mock data
        
        return "admin/vehicles-management";
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        List<?> allUsers = userService.getAllUsers();
        
        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("activeUsers", allUsers.size()); // Mock data
        model.addAttribute("pendingUsers", 0); // Mock data
        model.addAttribute("suspendedUsers", 0); // Mock data
        
        return "admin/users-management";
    }

    @GetMapping("/admin/reports")
    public String adminReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return "redirect:/login";
        }
        
        List<?> allBookings = bookingService.getAllBookings();
        List<?> allUsers = userService.getAllUsers();
        
        model.addAttribute("totalRevenue", bookingService.getTotalRevenue());
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("activeUsers", allUsers.size());
        
        return "admin/reports";
    }
    
    // API Endpoints for Real-time Data
    
    @GetMapping("/admin/api/analytics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        try {
            Map<String, Object> analytics = analyticsService.getDashboardAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // API endpoint removed - using server-side rendering

    // Add User Form Submission (Server-side)
    @PostMapping("/admin/users/add")
    public String addUser(@RequestParam Map<String, String> params, 
                         HttpSession session, 
                         RedirectAttributes redirectAttributes) {
        System.out.println("Add user form submitted");
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "No user in session");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        try {
            // Validate required fields
            if (params.get("firstName") == null || params.get("firstName").trim().isEmpty() ||
                params.get("lastName") == null || params.get("lastName").trim().isEmpty() ||
                params.get("username") == null || params.get("username").trim().isEmpty() ||
                params.get("email") == null || params.get("email").trim().isEmpty() ||
                params.get("password") == null || params.get("password").trim().isEmpty() ||
                params.get("role") == null || params.get("role").trim().isEmpty()) {
                
                redirectAttributes.addFlashAttribute("error", "All required fields must be filled");
                return "redirect:/admin/dashboard?tab=users";
            }
            
            // Check if username already exists
            if (userService.existsByUsername(params.get("username"))) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/admin/dashboard?tab=users";
            }
            
            // Check if email already exists
            if (userService.existsByEmail(params.get("email"))) {
                redirectAttributes.addFlashAttribute("error", "Email already exists");
                return "redirect:/admin/dashboard?tab=users";
            }
            
            // Create new user
            User newUser = new User();
            newUser.setId(java.util.UUID.randomUUID().toString()); // Generate UUID for ID
            newUser.setFirstName(params.get("firstName"));
            newUser.setLastName(params.get("lastName"));
            newUser.setUsername(params.get("username"));
            newUser.setEmail(params.get("email"));
            newUser.setPhoneNumber(params.get("phoneNumber"));
            newUser.setPassword(passwordEncoder.encode(params.get("password")));
            newUser.setStatus(params.get("status"));
            newUser.setCreatedDate(LocalDateTime.now());
            
            // Set normalized fields
            newUser.setNormalizedUserName(params.get("username").toUpperCase());
            newUser.setNormalizedEmail(params.get("email").toUpperCase());
            newUser.setEmailVerifed(false);
            newUser.setSecurityStamp(java.util.UUID.randomUUID().toString());
            newUser.setConcurrencyStamp(java.util.UUID.randomUUID().toString());
            
            // Set role
            Optional<Role> roleOpt = roleRepository.findByRoleName(params.get("role"));
            if (roleOpt.isPresent()) {
                newUser.setRoleId(roleOpt.get().getRoleId());
                newUser.setRole(roleOpt.get());
            } else {
                // Default to Customer role if role not found
                Optional<Role> customerRole = roleRepository.findByRoleName("Customer");
                if (customerRole.isPresent()) {
                    newUser.setRoleId(customerRole.get().getRoleId());
                    newUser.setRole(customerRole.get());
                }
            }
            
            // Save user
            userService.save(newUser);
            
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
            System.out.println("User added successfully: " + newUser.getUsername());
            
        } catch (Exception e) {
            logger.error("Error adding user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error adding user: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard?tab=users";
    }

    // Edit User Form
    @GetMapping("/admin/users/edit/{id}")
    public String editUserForm(@PathVariable String id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return "redirect:/admin/dashboard?tab=users";
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            return "redirect:/admin/dashboard?tab=users";
        }
        
        try {
            User user = userService.findById(id);
            if (user != null) {
                model.addAttribute("editUser", user);
                model.addAttribute("tab", "users");
                return "admin/edit-user";
            } else {
                return "redirect:/admin/dashboard?tab=users";
            }
        } catch (Exception e) {
            logger.error("Error loading user for edit: " + e.getMessage(), e);
            return "redirect:/admin/dashboard?tab=users";
        }
    }

    // Update User
    @PostMapping("/admin/users/update")
    public String updateUser(@RequestParam Map<String, String> params, 
                            HttpSession session, 
                            RedirectAttributes redirectAttributes) {
        System.out.println("Update user form submitted");
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "No user in session");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        try {
            String userId = params.get("userId");
            User user = userService.findById(userId);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/admin/dashboard?tab=users";
            }
            
            // Update user fields
            user.setFirstName(params.get("firstName"));
            user.setLastName(params.get("lastName"));
            user.setUsername(params.get("username"));
            user.setEmail(params.get("email"));
            user.setPhoneNumber(params.get("phoneNumber"));
            user.setStatus(params.get("status"));
            
            // Update password if provided
            if (params.get("password") != null && !params.get("password").trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(params.get("password")));
            }
            
            // Update role
            Optional<Role> roleOpt = roleRepository.findByRoleName(params.get("role"));
            if (roleOpt.isPresent()) {
                user.setRoleId(roleOpt.get().getRoleId());
                user.setRole(roleOpt.get());
            }
            
            // Save user
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
            System.out.println("User updated successfully: " + user.getUsername());
            
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Invalid user ID format");
        } catch (Exception e) {
            logger.error("Error updating user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard?tab=users";
    }

    // Delete User
    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable String id, 
                           HttpSession session, 
                           RedirectAttributes redirectAttributes) {
        System.out.println("Delete user requested for ID: " + id);
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "No user in session");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            redirectAttributes.addFlashAttribute("error", "Admin role required");
            return "redirect:/admin/dashboard?tab=users";
        }
        
        try {
            User user = userService.findById(id);
            if (user != null) {
                userService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
                System.out.println("User deleted successfully: " + user.getUsername());
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting user: " + e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard?tab=users";
    }
    
    @GetMapping("/test/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testUsers() {
        try {
            List<?> allUsers = userService.getAllUsersWithRole();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", allUsers);
            response.put("totalUsers", allUsers.size());
            
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            logger.error("Error occurred", e);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    
    
    
    
    @GetMapping("/admin/api/realtime")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRealTimeData() {
        try {
            Map<String, Object> realTimeData = analyticsService.getRealTimeData();
            return ResponseEntity.ok(realTimeData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/admin/api/performance")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            Map<String, Object> metrics = analyticsService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/admin/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = analyticsService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // User Management API
    
    @PostMapping("/admin/api/users/suspend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> suspendUser(@RequestParam String userId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            boolean success = userService.suspendUser(userId);
            return ResponseEntity.ok(Map.of("success", success));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/users/activate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activateUser(@RequestParam String userId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            boolean success = userService.activateUser(userId);
            return ResponseEntity.ok(Map.of("success", success));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Booking Management API
    
    @PostMapping("/admin/api/bookings/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBookingStatus(
            @RequestParam String bookingId, 
            @RequestParam String status, 
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Object updatedBooking = bookingService.updateBookingStatus(bookingId, status);
            if (updatedBooking != null) {
                return ResponseEntity.ok(Map.of("success", true, "booking", updatedBooking));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Vehicle Management API
    
    @PostMapping("/admin/api/vehicles/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateVehicleStatus(
            @RequestParam String vehicleId, 
            @RequestParam String status, 
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            Object updatedVehicle = vehicleService.updateVehicleStatus(vehicleId, status);
            if (updatedVehicle != null) {
                return ResponseEntity.ok(Map.of("success", true, "vehicle", updatedVehicle));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Vehicle not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Discount Management API
    
    @PostMapping("/admin/api/discounts/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createDiscount(
            @RequestParam String discountName,
            @RequestParam String description,
            @RequestParam String discountType,
            @RequestParam Double discountValue,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String voucherCode,
            @RequestParam Double minOrderAmount,
            @RequestParam(required = false) Double maxDiscountAmount,
            @RequestParam(required = false) Integer usageLimit,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement discount creation
            return ResponseEntity.ok(Map.of("success", true, "message", "Discount creation not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/discounts/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDiscount(
            @RequestParam String discountId,
            @RequestParam String discountName,
            @RequestParam String description,
            @RequestParam String discountType,
            @RequestParam Double discountValue,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String voucherCode,
            @RequestParam Double minOrderAmount,
            @RequestParam(required = false) Double maxDiscountAmount,
            @RequestParam(required = false) Integer usageLimit,
            @RequestParam Boolean isActive,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement discount update
            return ResponseEntity.ok(Map.of("success", true, "message", "Discount update not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/discounts/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDiscount(
            @RequestParam String discountId,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement discount deletion
            return ResponseEntity.ok(Map.of("success", true, "message", "Discount deletion not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Insurance Management API
    
    @PostMapping("/admin/api/insurance/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createInsurance(
            @RequestParam String insuranceName,
            @RequestParam String insuranceType,
            @RequestParam Double baseRatePerDay,
            @RequestParam(required = false) Double percentageRate,
            @RequestParam Double coverageAmount,
            @RequestParam(required = false) String applicableVehicleSeats,
            @RequestParam(required = false) String description,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement insurance creation
            return ResponseEntity.ok(Map.of("success", true, "message", "Insurance creation not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/insurance/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInsurance(
            @RequestParam String insuranceId,
            @RequestParam String insuranceName,
            @RequestParam String insuranceType,
            @RequestParam Double baseRatePerDay,
            @RequestParam(required = false) Double percentageRate,
            @RequestParam Double coverageAmount,
            @RequestParam(required = false) String applicableVehicleSeats,
            @RequestParam(required = false) String description,
            @RequestParam Boolean isActive,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement insurance update
            return ResponseEntity.ok(Map.of("success", true, "message", "Insurance update not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/insurance/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteInsurance(
            @RequestParam String insuranceId,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement insurance deletion
            return ResponseEntity.ok(Map.of("success", true, "message", "Insurance deletion not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Notification Management API
    
    @PostMapping("/admin/api/notifications/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestParam String userId,
            @RequestParam String message,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement notification sending
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification sending not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/notifications/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @RequestParam String notificationId,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement notification deletion
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification deletion not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Contract Management API
    
    @PostMapping("/admin/api/contracts/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateContractStatus(
            @RequestParam String contractId,
            @RequestParam String status,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement contract status update
            return ResponseEntity.ok(Map.of("success", true, "message", "Contract status update not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Payment Management API
    
    @PostMapping("/admin/api/payments/refund")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refundPayment(
            @RequestParam String paymentId,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null || !userService.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            // TODO: Implement payment refund
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment refund not yet implemented"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/admin/api/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId, HttpSession session) {
        System.out.println("Get user API called for ID: " + userId);
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            System.out.println("No user in session");
            return ResponseEntity.status(401).body(Map.of("error", "No user in session"));
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        System.out.println("Current user role: " + roleName);
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            System.out.println("User is not admin");
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
        }
        
        try {
            System.out.println("Looking for user with ID: " + userId);
            User user = userService.findByIdWithRole(userId);
            if (user == null) {
                System.out.println("User not found with ID: " + userId);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("User found: " + user.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId() != null ? user.getId() : "",
                    "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                    "lastName", user.getLastName() != null ? user.getLastName() : "",
                    "username", user.getUsername() != null ? user.getUsername() : "",
                    "email", user.getEmail() != null ? user.getEmail() : "",
                    "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    "roleName", user.getRoleName() != null ? user.getRoleName() : "",
                    "status", user.isActive() ? "Active" : "Inactive"
                )
            ));
        } catch (Exception e) {
            System.out.println("Error getting user: " + e.getMessage());
            logger.error("Error occurred", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Edit user
     */
    @PostMapping("/admin/api/users/edit")
    public ResponseEntity<Map<String, Object>> editUser(@RequestParam Map<String, String> params, HttpSession session) {
        System.out.println("Edit user API called");
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No user in session"));
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        System.out.println("Current user role: " + roleName);
        System.out.println("Current user: " + currentUser.getEmail());
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required. Current role: " + roleName));
        }
        
        try {
            String userId = params.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));
            }
            
            // Get existing user
            User existingUser = userService.findByIdWithRole(userId);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate required fields
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            String username = params.get("username");
            String email = params.get("email");
            String phoneNumber = params.get("phoneNumber");
            String role = params.get("role");
            String status = params.get("status");
            
            if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phoneNumber == null || phoneNumber.trim().isEmpty() ||
                role == null || role.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }
            
            // Check if username exists (excluding current user)
            User userWithUsername = userService.findByUsername(username);
            if (userWithUsername != null && !userWithUsername.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }
            
            // Check if email exists (excluding current user)
            User userWithEmail = userService.findByEmail(email);
            if (userWithEmail != null && !userWithEmail.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }
            
            // Set role
            Role userRole = roleService.findByRoleName(role).orElse(null);
            if (userRole == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            
            // Update user
            existingUser.setFirstName(firstName.trim());
            existingUser.setLastName(lastName.trim());
            existingUser.setUsername(username.trim());
            existingUser.setEmail(email.trim());
            existingUser.setPhoneNumber(phoneNumber.trim());
            existingUser.setRoleId(userRole.getRoleId());
            existingUser.setActive("Active".equals(status));
            
            // Update password if provided
            String password = params.get("password");
            if (password != null && !password.trim().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(password));
            }
            
            // Save user
            User updatedUser = userService.updateUser(existingUser);
            
            if (updatedUser != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "User updated successfully",
                    "userId", updatedUser.getId()
                ));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update user"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Add new user
     */
    @PostMapping("/admin/api/users/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam String userId, HttpSession session) {
        System.out.println("Delete user API called for ID: " + userId);
        User currentUser = (User) session.getAttribute("currentUser");
        
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No user in session"));
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
        }
        
        try {
            // Check if user exists
            User userToDelete = userService.findByIdWithRole(userId);
            if (userToDelete == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            // Delete user
            boolean success = userService.deleteUser(userId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "User deleted successfully"
                ));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to delete user"));
            }
            
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            logger.error("Error occurred", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/admin/api/users/add")
    public ResponseEntity<Map<String, Object>> addUser(@RequestParam Map<String, String> params, HttpSession session) {
        System.out.println("Add user API called");
        User currentUser = (User) session.getAttribute("currentUser");
        System.out.println("Current user in session: " + (currentUser != null ? currentUser.getEmail() : "null"));
        
        if (currentUser == null) {
            System.out.println("No user in session");
            return ResponseEntity.status(401).body(Map.of("error", "No user in session"));
        }
        
        // Check if user has admin role
        String roleName = currentUser.getRoleName();
        System.out.println("User role: " + roleName);
        
        if (roleName == null || !"Admin".equalsIgnoreCase(roleName)) {
            System.out.println("User is not admin: " + roleName);
            return ResponseEntity.status(403).body(Map.of("error", "Admin role required"));
        }
        
        try {
            // Validate required fields
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            String username = params.get("username");
            String email = params.get("email");
            String phoneNumber = params.get("phoneNumber");
            String password = params.get("password");
            String role = params.get("role");
            String status = params.get("status");
            
            if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phoneNumber == null || phoneNumber.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }
            
            // Check if username or email already exists
            if (userService.findByUsername(username) != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
            }
            
            if (userService.findByEmail(email) != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }
            
            // Set role first
            Role userRole = roleService.findByRoleName(role).orElse(null);
            if (userRole == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            
            // Create new user
            User newUser = new User();
            newUser.setFirstName(firstName.trim());
            newUser.setLastName(lastName.trim());
            newUser.setUsername(username.trim());
            newUser.setEmail(email.trim());
            newUser.setPhoneNumber(phoneNumber.trim());
            newUser.setPassword(password); // Don't encode here, register method will do it
            newUser.setRoleId(userRole.getRoleId());
            
            // Set status
            newUser.setStatus(status);
            
            System.out.println("Creating user with data:");
            System.out.println("  FirstName: " + newUser.getFirstName());
            System.out.println("  LastName: " + newUser.getLastName());
            System.out.println("  Username: " + newUser.getUsername());
            System.out.println("  Email: " + newUser.getEmail());
            System.out.println("  Phone: " + newUser.getPhoneNumber());
            System.out.println("  Role: " + newUser.getRoleId());
            System.out.println("  Status: " + newUser.getStatus());
            
            // Save user using register method
            boolean success = userService.register(newUser);
            
            if (success) {
                // Get the created user to return the ID
                User createdUser = userService.findByEmail(email);
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "User created successfully",
                    "userId", createdUser != null ? createdUser.getId() : "unknown"
                ));
            } else {
                return ResponseEntity.internalServerError().body(Map.of("error", "Failed to create user"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
