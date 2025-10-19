package com.ecodana.evodanavn1.controller.owner;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.repository.TransmissionTypeRepository;
import com.ecodana.evodanavn1.repository.VehicleCategoriesRepository;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final UserService userService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final TransmissionTypeRepository transmissionTypeRepository;
    private final VehicleCategoriesRepository vehicleCategoriesRepository;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_key:}")
    private String cloudApiKey;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_secret:}")
    private String cloudApiSecret;

    @Autowired
    public OwnerController(UserService userService, VehicleService vehicleService, BookingService bookingService, TransmissionTypeRepository transmissionTypeRepository, VehicleCategoriesRepository vehicleCategoriesRepository) {
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.transmissionTypeRepository = transmissionTypeRepository;
        this.vehicleCategoriesRepository = vehicleCategoriesRepository;
    }

    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        if (!userService.isOwner(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Owner role required.");
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("totalVehicles", vehicleService.getAllVehicles().size());
        model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles().size());
        // Add more stats as needed for the overview page

        return "owner/dashboard";
    }

    @GetMapping("/cars")
    public String carsPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("currentPage", "cars");
        
        // Lấy current user
        User currentUser = (User) session.getAttribute("currentUser");
        
        // Lấy chỉ vehicles của owner hiện tại
        List<Vehicle> allVehicles = vehicleService.getAllVehicles().stream()
                .filter(v -> currentUser != null && currentUser.getId().equals(v.getOwnerId()))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("vehicles", allVehicles);
        
        // Đếm số lượng theo từng status
        long pendingApprovalCount = allVehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.PendingApproval)
                .count();
        long availableCount = allVehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.Available)
                .count();
        long rentedCount = allVehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.Rented)
                .count();
        long maintenanceCount = allVehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.Maintenance)
                .count();
        long unavailableCount = allVehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.Unavailable)
                .count();
        
        model.addAttribute("pendingApprovalCount", pendingApprovalCount);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("rentedCount", rentedCount);
        model.addAttribute("maintenanceCount", maintenanceCount);
        model.addAttribute("unavailableCount", unavailableCount);
        
        model.addAttribute("transmissions", transmissionTypeRepository.findAll());
        model.addAttribute("categories", vehicleCategoriesRepository.findAll());
        Map<String, String> transmissionMap = new HashMap<>();
        transmissionTypeRepository.findAll().forEach(t -> transmissionMap.put(t.getTransmissionTypeId().toString(), t.getTransmissionTypeName()));
        model.addAttribute("transmissionMap", transmissionMap);
        Map<Integer, String> categoryMap = new HashMap<>();
        vehicleCategoriesRepository.findAll().forEach(c -> categoryMap.put(c.getCategoryId(), c.getCategoryName()));
        model.addAttribute("categoryMap", categoryMap);

        return "owner/cars-management";
    }

    @GetMapping("/bookings")
    public String bookingsPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("currentPage", "bookings");
        
        // Lấy tất cả bookings
        List<Booking> allBookings = bookingService.getAllBookings();
        model.addAttribute("bookings", allBookings);
        
        // Đếm số lượng theo từng status
        long pendingCount = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Pending)
                .count();
        long approvedCount = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Approved ||
                             b.getStatus() == Booking.BookingStatus.AwaitingDeposit)
                .count();
        long ongoingCount = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Confirmed ||
                             b.getStatus() == Booking.BookingStatus.Ongoing)
                .count();
        long completedCount = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Completed)
                .count();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedCount", completedCount);
        
        // Lấy danh sách pending bookings cho notification badge
        List<Booking> pendingBookings = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Pending)
                .collect(Collectors.toList());
        model.addAttribute("pendingBookings", pendingBookings);

        return "owner/bookings-management";
    }

    @GetMapping("/customers")
    public String customersPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("currentPage", "customers");
        List<Booking> bookings = bookingService.getAllBookings();
        Map<User, Long> customerBookingCounts = bookings.stream()
                .filter(b -> b.getUser() != null)
                .collect(Collectors.groupingBy(Booking::getUser, Collectors.counting()));

        List<Map<String, Object>> customers = customerBookingCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("user", entry.getKey());
                    customerMap.put("bookingCount", entry.getValue());
                    return customerMap;
                })
                .collect(Collectors.toList());

        model.addAttribute("customers", customers);

        return "owner/customers-management";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        
        model.addAttribute("currentPage", "profile");
        // Current user is already added in checkAuthentication
        return "owner/profile-management";
    }


    @PostMapping("/cars")
    public String addCar(@RequestParam Map<String, String> carData,
                         @RequestParam(value = "images", required = false) MultipartFile[] images,
                         HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleId(java.util.UUID.randomUUID().toString());
            vehicle.setVehicleModel(carData.get("model"));
            vehicle.setVehicleType(Vehicle.VehicleType.valueOf(carData.get("type")));
            if (carData.get("transmissionTypeId") != null && !carData.get("transmissionTypeId").isEmpty()) {
                transmissionTypeRepository.findById(Integer.parseInt(carData.get("transmissionTypeId"))).ifPresent(vehicle::setTransmissionType);
            }
            if (carData.get("categoryId") != null && !carData.get("categoryId").isEmpty()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(carData.get("categoryId"))).ifPresent(vehicle::setCategory);
            }
            vehicle.setLicensePlate(carData.get("licensePlate"));
            if (carData.get("yearManufactured") != null && !carData.get("yearManufactured").isEmpty()) {
                vehicle.setYearManufactured(Integer.parseInt(carData.get("yearManufactured")));
            }
            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));
            
            // Fix JSON format - values should be numbers, not strings
            String hourlyRate = carData.getOrDefault("hourlyRate", "0");
            String dailyRate = carData.getOrDefault("dailyRate", "0");
            String monthlyRate = carData.getOrDefault("monthlyRate", "0");
            vehicle.setRentalPrices(String.format("{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}", 
                hourlyRate.isEmpty() ? "0" : hourlyRate, 
                dailyRate.isEmpty() ? "0" : dailyRate, 
                monthlyRate.isEmpty() ? "0" : monthlyRate));
            if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
            }
            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
            
            // Set status to PendingApproval - chờ admin duyệt
            vehicle.setStatus(Vehicle.VehicleStatus.PendingApproval);
            vehicle.setCreatedDate(java.time.LocalDateTime.now());
            
            // Set owner ID
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                vehicle.setOwnerId(currentUser.getId());
            }

            if (images != null && images.length > 0 && !images[0].isEmpty()) {
                if (cloudName != null && !cloudName.isBlank()) {
                    try {
                        com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(Map.of("cloud_name", cloudName, "api_key", cloudApiKey, "api_secret", cloudApiSecret));
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(images[0].getBytes(), Map.of("folder", "ecodana/vehicles"));
                        vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                    } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Image upload failed: Cloudinary credentials are not configured.");
                }
            }

            vehicleService.saveVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Xe đã được thêm thành công! Vui lòng chờ admin duyệt để xe có thể cho thuê.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm xe thất bại: " + e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    @PostMapping("/cars/{id}")
    public String updateCar(@PathVariable String id, @RequestParam Map<String, String> carData,
                            @RequestParam(value = "images", required = false) MultipartFile[] images,
                            HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        try {
            java.util.Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);
            if (vehicleOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found.");
                return "redirect:/owner/cars";
            }

            Vehicle vehicle = vehicleOptional.get();
            vehicle.setVehicleModel(carData.get("model"));
            vehicle.setVehicleType(Vehicle.VehicleType.valueOf(carData.get("type")));

            String transmissionTypeId = carData.get("transmissionTypeId");
            if (transmissionTypeId != null && !transmissionTypeId.isBlank()) {
                transmissionTypeRepository.findById(Integer.parseInt(transmissionTypeId)).ifPresent(vehicle::setTransmissionType);
            }

            String categoryId = carData.get("categoryId");
            if (categoryId != null && !categoryId.isBlank()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(categoryId)).ifPresent(vehicle::setCategory);
            }

            vehicle.setLicensePlate(carData.get("licensePlate"));

            String yearManufactured = carData.get("yearManufactured");
            if (yearManufactured != null && !yearManufactured.isBlank()) {
                vehicle.setYearManufactured(Integer.parseInt(yearManufactured));
            }

            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

            String hourlyRate = carData.getOrDefault("hourlyRate", "0").isBlank() ? "0" : carData.get("hourlyRate");
            String dailyRate = carData.getOrDefault("dailyRate", "0").isBlank() ? "0" : carData.get("dailyRate");
            String monthlyRate = carData.getOrDefault("monthlyRate", "0").isBlank() ? "0" : carData.get("monthlyRate");
            vehicle.setRentalPrices(String.format("{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}", hourlyRate, dailyRate, monthlyRate));

            String batteryCapacity = carData.get("batteryCapacity");
            if (batteryCapacity != null && !batteryCapacity.isBlank()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(batteryCapacity));
            }

            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));

            if (carData.containsKey("status")) {
                vehicle.setStatus(Vehicle.VehicleStatus.valueOf(carData.get("status")));
            }

            if (images != null && images.length > 0 && !images[0].isEmpty()) {
                if (cloudName != null && !cloudName.isBlank()) {
                    try {
                        com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(Map.of("cloud_name", cloudName, "api_key", cloudApiKey, "api_secret", cloudApiSecret));
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(images[0].getBytes(), Map.of("folder", "ecodana/vehicles"));
                        vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                    } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                    }
                } else {
                     redirectAttributes.addFlashAttribute("warning", "Image could not be uploaded: Cloudinary credentials not configured.");
                }
            }

            vehicleService.updateVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");

        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update vehicle: Invalid number format provided.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update vehicle: " + e.getMessage());
        }

        return "redirect:/owner/cars";
    }

    @PostMapping("/cars/{id}/toggle-availability")
    public String toggleAvailability(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        try {
            vehicleService.getVehicleById(id).ifPresent(vehicle -> {
                vehicle.setStatus(vehicle.getStatus() == Vehicle.VehicleStatus.Available ? Vehicle.VehicleStatus.Unavailable : Vehicle.VehicleStatus.Available);
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle status updated");
            });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/owner/cars";
    }

    @DeleteMapping("/cars/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCar(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isOwner(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Vehicle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to delete vehicle: " + e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam Map<String, String> ownerUpdate,
                                HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            User userToUpdate = userService.getUserWithRole(currentUser.getEmail());
            if (userToUpdate != null) {
                userToUpdate.setFirstName(ownerUpdate.get("firstName"));
                userToUpdate.setLastName(ownerUpdate.get("lastName"));
                userToUpdate.setPhoneNumber(ownerUpdate.get("phoneNumber"));
                userService.updateUser(userToUpdate);
                session.setAttribute("currentUser", userToUpdate);
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/owner/profile";
    }

    @PostMapping("/bookings/{id}/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBooking(@PathVariable("id") String bookingId, @RequestBody Map<String, String> data, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !userService.isOwner(currentUser)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }
        try {
            Booking updatedBooking = bookingService.updateBookingDetails(bookingId, data);
            if (updatedBooking != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Booking updated successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to update booking: " + e.getMessage()));
        }
    }

    @GetMapping("/management/bookings/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookingDetail(@PathVariable String id) {
        // Auth check can be added here if needed
        try {
            return bookingService.findById(id).map(booking -> {
                Map<String, Object> bookingData = new HashMap<>();
                bookingData.put("bookingId", booking.getBookingId());
                bookingData.put("bookingCode", booking.getBookingCode());
                bookingData.put("status", booking.getStatus().name());
                bookingData.put("pickupDateTime", booking.getPickupDateTime());
                bookingData.put("returnDateTime", booking.getReturnDateTime());
                bookingData.put("totalAmount", booking.getTotalAmount());
                bookingData.put("rentalType", booking.getRentalType().name());
                bookingData.put("createdDate", booking.getCreatedDate());
                bookingData.put("paymentMethod", booking.getExpectedPaymentMethod());
                bookingData.put("cancelReason", booking.getCancelReason());

                if (booking.getUser() != null) {
                    bookingData.put("customerId", booking.getUser().getId());
                    bookingData.put("customerName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                    bookingData.put("customerEmail", booking.getUser().getEmail());
                    bookingData.put("customerPhone", booking.getUser().getPhoneNumber());
                    bookingData.put("customerDOB", booking.getUser().getUserDOB());
                }

                if (booking.getVehicle() != null) {
                    bookingData.put("vehicleModel", booking.getVehicle().getVehicleModel());
                    bookingData.put("licensePlate", booking.getVehicle().getLicensePlate());
                    bookingData.put("vehicleCategory", booking.getVehicle().getCategory() != null ?
                        booking.getVehicle().getCategory().getCategoryName() : "N/A");
                    bookingData.put("transmission", booking.getVehicle().getTransmissionType() != null ?
                        booking.getVehicle().getTransmissionType().getTransmissionTypeName() : "N/A");
                }

                if (booking.getDiscount() != null) {
                    bookingData.put("discount", booking.getDiscount().getDiscountName());
                }

                return ResponseEntity.ok(bookingData);
            }).orElse(ResponseEntity.status(404).body(Map.of("status", "error", "message", "Booking not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to load booking: " + e.getMessage()));
        }
    }

    @PostMapping("/management/bookings/{id}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveBookingManagement(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated"));
        }

        try {
            Booking booking = bookingService.approveBooking(id, currentUser);
            if (booking != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Booking approved successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to approve booking: " + e.getMessage()));
        }
    }

    @PostMapping("/management/bookings/{id}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectBookingManagement(@PathVariable String id,
                                                                        @RequestBody Map<String, String> payload,
                                                                        HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated"));
        }

        try {
            String reason = payload.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Rejection reason is required"));
            }

            Booking booking = bookingService.rejectBooking(id, reason, currentUser);
            if (booking != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Booking rejected successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to reject booking: " + e.getMessage()));
        }
    }

    @PostMapping("/management/bookings/{id}/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeBookingManagement(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated"));
        }

        try {
            Booking booking = bookingService.completeBooking(id);
            if (booking != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Booking marked as completed"));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to complete booking: " + e.getMessage()));
        }
    }

    @PostMapping("/management/bookings/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelBookingManagement(@PathVariable String id,
                                                                        @RequestBody Map<String, String> payload,
                                                                        HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isOwner(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied"));
        }

        try {
            String reason = payload.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "Cancellation reason is required"));
            }

            Booking booking = bookingService.cancelBooking(id, reason);
            if (booking != null) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", "Booking not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to cancel booking: " + e.getMessage()));
        }
    }

    @GetMapping("/management/customer/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerProfile(@PathVariable String id) {
        // Auth check can be added here if needed
        try {
            User customer = userService.findById(id);
            if (customer == null) {
                return ResponseEntity.status(404).body(Map.of("status", "error", "message", "Customer not found"));
            }

            Map<String, Object> customerData = new HashMap<>();
            customerData.put("userId", customer.getId());
            customerData.put("username", customer.getUsername());
            customerData.put("fullName", customer.getFirstName() + " " + customer.getLastName());
            customerData.put("email", customer.getEmail());
            customerData.put("phoneNumber", customer.getPhoneNumber());
            customerData.put("dateOfBirth", customer.getUserDOB());
            customerData.put("gender", customer.getGender() != null ? customer.getGender().name() : "N/A");
            customerData.put("avatarUrl", customer.getAvatarUrl());
            customerData.put("status", customer.getStatus().name());
            customerData.put("role", customer.getRoleName());
            customerData.put("createdDate", customer.getCreatedDate());

            List<Booking> customerBookings = bookingService.getBookingsByUser(customer);
            customerData.put("totalBookings", customerBookings.size());
            customerData.put("completedBookings", customerBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Completed).count());
            customerData.put("cancelledBookings", customerBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Cancelled).count());

            return ResponseEntity.ok(customerData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to load customer profile: " + e.getMessage()));
        }
    }
}
