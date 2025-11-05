package com.ecodana.evodanavn1.controller.admin;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.UserFeedback;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.model.Notification;
import com.ecodana.evodanavn1.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    private NotificationService notificationService;
    @Autowired
    private UserFeedbackService userFeedbackService;
    @GetMapping({"/admin", "/admin/dashboard"})
    public String adminDashboard(@RequestParam(required = false) String tab, 
                                  @RequestParam(required = false) String roleFilter,
                                  HttpSession session, Model model, HttpServletResponse response) {
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
            
            // Log all users and their roles for debugging
            logger.info("Total users loaded: {}", allUsers.size());
            logger.info("roleFilter parameter: '{}'", roleFilter);
            
            // Filter users by role if roleFilter parameter is provided
            List<User> filteredUsers;
            if (roleFilter != null && !roleFilter.trim().isEmpty()) {
                logger.info("Filtering users by role: '{}'", roleFilter);
                filteredUsers = allUsers.stream()
                    .filter(u -> {
                        String userRole = u.getRole() != null ? u.getRole().getRoleName() : "Customer";
                        boolean matches = userRole.equalsIgnoreCase(roleFilter.trim());
                        if (matches) {
                            logger.info("User '{}' has role '{}' - MATCH", u.getUsername(), userRole);
                        }
                        return matches;
                    })
                    .collect(java.util.stream.Collectors.toList());
                logger.info("Filtered {} users out of {} total users", filteredUsers.size(), allUsers.size());
            } else {
                logger.info("No filter applied - showing all users");
                filteredUsers = allUsers;
            }
            
            model.addAttribute("users", filteredUsers.size() > 100 ? filteredUsers.subList(0, 100) : filteredUsers);
            model.addAttribute("roleFilter", roleFilter);
            List<Vehicle> allVehicles = vehicleService.getAllVehicles();
            model.addAttribute("vehicles", allVehicles.size() > 100 ? allVehicles.subList(0, 100) : allVehicles);
            List<?> allBookings = bookingService.getAllBookings();
            model.addAttribute("bookings", allBookings.size() > 100 ? allBookings.subList(0, 100) : allBookings);
            model.addAttribute("discounts", List.of());
            model.addAttribute("insuranceList", List.of());
            model.addAttribute("contracts", List.of());
            model.addAttribute("payments", List.of());

            // Lấy danh sách thông báo 1 LẦN DUY NHẤT
            List<Notification> allNotifications = notificationService.getNotificationsByUserId(userWithRole.getId());
            model.addAttribute("notifications", allNotifications);
            model.addAttribute("recentNotifications", allNotifications);
            // Tính toán số lượng thông báo "Today" trong Java
            long notificationsTodayCount = 0;
            if (allNotifications != null) {
                LocalDate today = LocalDate.now();
                notificationsTodayCount = allNotifications.stream()
                        .filter(n -> n.getCreatedDate() != null && n.getCreatedDate().toLocalDate().isEqual(today))
                        .count();
            }
            // Gửi biến đã tính toán sang view
            model.addAttribute("notificationsTodayCount", notificationsTodayCount);

            List<UserFeedback> allFeedback = userFeedbackService.getAllFeedback();
            List<UserFeedback> feedbackWithReplies = allFeedback.stream()
                    .filter(f -> f.getStaffReply() != null && !f.getStaffReply().trim().isEmpty())
                    .toList();
            List<UserFeedback> feedbackWithoutReplies = allFeedback.stream()
                    .filter(f -> f.getStaffReply() == null || f.getStaffReply().trim().isEmpty())
                    .toList();

            model.addAttribute("allFeedback", allFeedback);
            model.addAttribute("feedbackWithReplies", feedbackWithReplies);
            model.addAttribute("feedbackWithoutReplies", feedbackWithoutReplies);

            model.addAttribute("user", userWithRole);
            model.addAttribute("tab", tab != null ? tab : "overview");
            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("totalBookings", allBookings.size());
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("filteredUserCount", filteredUsers.size());

            // Add user statistics
            Map<String, Object> userStats = userService.getUserStatistics();
            model.addAttribute("activeUsers", userStats.get("activeUsers"));
            model.addAttribute("inactiveUsers", userStats.get("inactiveUsers"));
            model.addAttribute("bannedUsers", userStats.get("bannedUsers"));
            
            // Add role counts
            long adminCount = allUsers.stream().filter(u -> u.getRole() != null && "Admin".equals(u.getRole().getRoleName())).count();
            long ownerCount = allUsers.stream().filter(u -> u.getRole() != null && "Owner".equals(u.getRole().getRoleName())).count();
            long customerCount = allUsers.stream().filter(u -> u.getRole() == null || "Customer".equals(u.getRole().getRoleName())).count();
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("ownerCount", ownerCount);
            model.addAttribute("customerCount", customerCount);

            // Add vehicle statistics
            long pendingApprovalVehicles = allVehicles.stream().filter(v -> "PendingApproval".equals(v.getStatus().name())).count();
            long availableVehicles = allVehicles.stream().filter(v -> "Available".equals(v.getStatus().name())).count();
            long rentedVehicles = allVehicles.stream().filter(v -> "Rented".equals(v.getStatus().name())).count();
            long maintenanceVehicles = allVehicles.stream().filter(v -> "Maintenance".equals(v.getStatus().name())).count();
            model.addAttribute("pendingApprovalVehicles", pendingApprovalVehicles);
            model.addAttribute("availableVehicles", availableVehicles);
            model.addAttribute("rentedVehicles", rentedVehicles);
            model.addAttribute("maintenanceVehicles", maintenanceVehicles);

            return "admin/admin-dashboard";
        } catch (Exception e) {
            logger.error("Error loading admin data: " + e.getMessage(), e);
            // Fallback with empty data
            model.addAttribute("user", userWithRole);
            model.addAttribute("currentUser", userWithRole);
            model.addAttribute("vehicles", List.of());
            model.addAttribute("bookings", List.of());
            model.addAttribute("users", List.of());
            model.addAttribute("allFeedback", List.of());
            model.addAttribute("feedbackWithReplies", List.of());
            model.addAttribute("feedbackWithoutReplies", List.of());
            model.addAttribute("totalVehicles", 0);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("activeUsers", 0);
            model.addAttribute("inactiveUsers", 0);
            model.addAttribute("bannedUsers", 0);
            model.addAttribute("pendingApprovalVehicles", 0);
            model.addAttribute("availableVehicles", 0);
            model.addAttribute("rentedVehicles", 0);
            model.addAttribute("maintenanceVehicles", 0);
            model.addAttribute("tab", tab != null ? tab : "overview");
            model.addAttribute("analytics", new HashMap<>());
            model.addAttribute("notificationsTodayCount", 0);
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
    // Vehicle listing moved to VehicleAdminController at /admin/vehicles
    // User management moved to UserAdminController at /admin/users
    // All /admin/users/* and /admin/vehicles/* endpoints are now handled by dedicated controllers
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
    // User CRUD Operations moved to UserAdminController
    // All /admin/users/* endpoints are now handled by UserAdminController

    // Vehicle CRUD Operations moved to VehicleAdminController
    // All /admin/vehicles/* endpoints (except /admin/vehicles listing) are now handled by VehicleAdminController
}
