package com.ecodana.evodanavn1.controller.owner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.TransmissionType;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.VehicleCategories;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;
import com.ecodana.evodanavn1.repository.VehicleCategoriesRepository;
import com.ecodana.evodanavn1.repository.TransmissionTypeRepository;


import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TransmissionTypeRepository transmissionTypeRepository;
    @Autowired
    private VehicleCategoriesRepository vehicleCategoriesRepository;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_key:}")
    private String cloudApiKey;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_secret:}")
    private String cloudApiSecret;

    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        if (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser) && !userService.isOwner(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Owner/Staff/Admin role required.");
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        User currentUser = (User) session.getAttribute("currentUser");
        User userWithRole = userService.getUserWithRole(currentUser.getEmail());
        if (userWithRole == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        try {
            model.addAttribute("user", userWithRole);
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
            model.addAttribute("bookings", bookingService.getAllBookings());
            model.addAttribute("transmissions", transmissionTypeRepository.findAll());
            model.addAttribute("categories", vehicleCategoriesRepository.findAll());
            model.addAttribute("totalVehicles", vehicleService.getAllVehicles().size());
            model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles().size());
            model.addAttribute("activeBookings", bookingService.getActiveBookings());
            model.addAttribute("pendingBookings", bookingService.getPendingBookings());
            model.addAttribute("todayRevenue", bookingService.getTodayRevenue());

            Map<String, String> transmissionMap = new HashMap<>();
            transmissionTypeRepository.findAll().forEach(t -> transmissionMap.put(t.getTransmissionTypeId().toString(), t.getTransmissionTypeName()));
            model.addAttribute("transmissionMap", transmissionMap);

            Map<Integer, String> categoryMap = new HashMap<>();
            vehicleCategoriesRepository.findAll().forEach(c -> categoryMap.put(c.getCategoryId(), c.getCategoryName()));
            model.addAttribute("categoryMap", categoryMap);

            return "owner/dashboard";
        } catch (Exception e) {
            return "owner/dashboard";
        }
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
            vehicle.setRentalPrices(String.format("{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}", carData.getOrDefault("hourlyRate", "0"), carData.getOrDefault("dailyRate", "0"), carData.getOrDefault("monthlyRate", "0")));
            if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
            }
            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
            vehicle.setStatus(Vehicle.VehicleStatus.valueOf(carData.getOrDefault("status", "Available")));
            vehicle.setCreatedDate(java.time.LocalDateTime.now());

            if (images != null && images.length > 0 && !images[0].isEmpty() && cloudName != null && !cloudName.isBlank()) {
                try {
                    com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(Map.of("cloud_name", cloudName, "api_key", cloudApiKey, "api_secret", cloudApiSecret));
                    Map uploadResult = cloudinary.uploader().upload(images[0].getBytes(), Map.of("folder", "ecodana/vehicles"));
                    vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                }
            }
            vehicleService.saveVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add vehicle: " + e.getMessage());
        }
        return "redirect:/owner/dashboard?section=cars";
    }

    @PutMapping("/cars/{id}")
    public String updateCar(@PathVariable String id, @RequestParam Map<String, String> carData,
                            @RequestParam(value = "images", required = false) MultipartFile[] images,
                            HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        try {
            vehicleService.getVehicleById(id).ifPresent(vehicle -> {
                vehicle.setVehicleModel(carData.get("model"));
                vehicle.setVehicleType(Vehicle.VehicleType.valueOf(carData.get("type")));
                if (carData.get("transmissionTypeId") != null && !carData.get("transmissionTypeId").isBlank()) {
                    transmissionTypeRepository.findById(Integer.parseInt(carData.get("transmissionTypeId"))).ifPresent(vehicle::setTransmissionType);
                }
                if (carData.get("categoryId") != null && !carData.get("categoryId").isBlank()) {
                    vehicleCategoriesRepository.findById(Integer.parseInt(carData.get("categoryId"))).ifPresent(vehicle::setCategory);
                }
                vehicle.setLicensePlate(carData.get("licensePlate"));
                if (carData.get("yearManufactured") != null && !carData.get("yearManufactured").isBlank()) {
                    vehicle.setYearManufactured(Integer.parseInt(carData.get("yearManufactured")));
                }
                vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
                vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));
                vehicle.setRentalPrices(String.format("{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}", carData.getOrDefault("hourlyRate", "0"), carData.getOrDefault("dailyRate", "0"), carData.getOrDefault("monthlyRate", "0")));
                if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                    vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
                }
                vehicle.setDescription(carData.get("description"));
                vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
                if (carData.containsKey("status")) {
                    vehicle.setStatus(Vehicle.VehicleStatus.valueOf(carData.get("status")));
                }
                if (images != null && images.length > 0 && !images[0].isEmpty() && cloudName != null && !cloudName.isBlank()) {
                    try {
                        com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(Map.of("cloud_name", cloudName, "api_key", cloudApiKey, "api_secret", cloudApiSecret));
                        Map uploadResult = cloudinary.uploader().upload(images[0].getBytes(), Map.of("folder", "ecodana/vehicles"));
                        vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                    } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                    }
                }
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
            });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update vehicle: " + e.getMessage());
        }
        return "redirect:/owner/dashboard?section=cars";
    }

    @PostMapping("/cars/{id}/toggle-availability")
    public String toggleAvailability(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;
        Optional<Vehicle> verhicles = vehicleService.getVehicleById(id);
        verhicles.ifPresent(vehicle -> {
                vehicle.setStatus(vehicle.getStatus() == Vehicle.VehicleStatus.Available ? Vehicle.VehicleStatus.Unavailable : Vehicle.VehicleStatus.Available);
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle status updated");
        });
        return "redirect:/owner/dashboard?section=cars";
    }

    @DeleteMapping("/cars/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCar(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Vehicle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to delete vehicle: " + e.getMessage()));
        }
    }

    @PostMapping("/bookings/{id}/accept")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptBooking(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }
        try {
            bookingService.findById(id).ifPresent(booking -> {
                booking.setStatus(Booking.BookingStatus.Approved);
                bookingService.updateBooking(booking);
            });
            return ResponseEntity.ok(Map.of("status", "success", "message", "Booking accepted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to accept booking: " + e.getMessage()));
        }
    }

    @PostMapping("/bookings/{id}/decline")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> declineBooking(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }
        try {
            bookingService.findById(id).ifPresent(booking -> {
                booking.setStatus(Booking.BookingStatus.Cancelled);
                bookingService.updateBooking(booking);
            });
            return ResponseEntity.ok(Map.of("status", "success", "message", "Booking declined successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to decline booking: " + e.getMessage()));
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
        return "redirect:/owner/dashboard?section=profile";
    }

    @GetMapping("/cars/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCarForEdit(@PathVariable String id, HttpSession session) {
        try {
            return vehicleService.getVehicleById(id).map(vehicle -> {
                Map<String, Object> car = new HashMap<>();
                car.put("id", vehicle.getVehicleId());
                car.put("model", vehicle.getVehicleModel());
                car.put("type", vehicle.getVehicleType().name());
                car.put("transmissionTypeId", vehicle.getTransmissionType() != null ? vehicle.getTransmissionType().getTransmissionTypeId() : null);
                car.put("categoryId", vehicle.getCategory() != null ? vehicle.getCategory().getCategoryId() : null);
                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());
                try {
                    Map<String, Object> prices = new ObjectMapper().readValue(vehicle.getRentalPrices(), new TypeReference<Map<String, Object>>() {});
                    car.put("dailyRate", prices.get("daily"));
                    car.put("hourlyRate", prices.get("hourly"));
                    car.put("monthlyRate", prices.get("monthly"));
                } catch (Exception e) {
                    car.put("dailyRate", 0); car.put("hourlyRate", 0); car.put("monthlyRate", 0);
                }
                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());
                car.put("status", vehicle.getStatus().name());
                car.put("yearManufactured", vehicle.getYearManufactured());
                return ResponseEntity.ok(Map.of("status", "success", "data", car));
            }).orElse(ResponseEntity.status(404).body(Map.of("status", "error", "message", "Vehicle not found")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Failed to get vehicle: " + e.getMessage()));
        }
    }

    // Booking Management Endpoints
    @GetMapping("/management")
    public String managementPage(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        User currentUser = (User) session.getAttribute("currentUser");
        try {
            java.util.List<Booking> allBookings = bookingService.getAllBookings();
            Map<String, Long> statusCounts = bookingService.getBookingCountsByStatus();

            model.addAttribute("bookings", allBookings);
            model.addAttribute("pendingBookings", bookingService.getPendingBookings());
            model.addAttribute("pendingCount", statusCounts.get("pending"));
            model.addAttribute("approvedCount", statusCounts.get("approved"));
            model.addAttribute("ongoingCount", statusCounts.get("ongoing"));
            model.addAttribute("completedCount", statusCounts.get("completed"));

            return "owner/owner-management";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to load management page: " + e.getMessage());
            return "redirect:/owner/dashboard";
        }
    }

    @GetMapping("/management/bookings/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookingDetail(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

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

                // Customer information
                if (booking.getUser() != null) {
                    bookingData.put("customerId", booking.getUser().getId());
                    bookingData.put("customerName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                    bookingData.put("customerEmail", booking.getUser().getEmail());
                    bookingData.put("customerPhone", booking.getUser().getPhoneNumber());
                    bookingData.put("customerDOB", booking.getUser().getUserDOB());
                }

                // Vehicle information
                if (booking.getVehicle() != null) {
                    bookingData.put("vehicleModel", booking.getVehicle().getVehicleModel());
                    bookingData.put("licensePlate", booking.getVehicle().getLicensePlate());
                    bookingData.put("vehicleCategory", booking.getVehicle().getCategory() != null ? 
                        booking.getVehicle().getCategory().getCategoryName() : "N/A");
                    bookingData.put("transmission", booking.getVehicle().getTransmissionType() != null ? 
                        booking.getVehicle().getTransmissionType().getTransmissionTypeName() : "N/A");
                }

                // Discount information
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
                                                                        @org.springframework.web.bind.annotation.RequestBody Map<String, String> payload,
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
                                                                        @org.springframework.web.bind.annotation.RequestBody Map<String, String> payload,
                                                                        HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser) && !userService.isOwner(currentUser))) {
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

    @GetMapping("/management/calendar-events")
    @ResponseBody
    public ResponseEntity<java.util.List<Map<String, Object>>> getCalendarEvents(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(403).body(new ArrayList<>());
        }

        try {
            java.util.List<Booking> bookings = bookingService.getAllBookings();
            java.util.List<Map<String, Object>> events = new ArrayList<>();

            for (Booking booking : bookings) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", booking.getBookingId());
                event.put("title", booking.getBookingCode() + " - " + 
                    (booking.getVehicle() != null ? booking.getVehicle().getVehicleModel() : "Vehicle"));
                event.put("start", booking.getPickupDateTime().toString());
                event.put("end", booking.getReturnDateTime().toString());
                event.put("status", booking.getStatus().name());
                event.put("className", "fc-event-" + booking.getStatus().name().toLowerCase());
                
                // Color coding based on status
                String color = switch (booking.getStatus()) {
                    case Pending -> "#fbbf24";
                    case Approved -> "#10b981";
                    case Ongoing -> "#3b82f6";
                    case Completed -> "#6b7280";
                    case Rejected, Cancelled -> "#ef4444";
                };
                event.put("backgroundColor", color);
                
                // Extended properties for timeline view
                Map<String, Object> extendedProps = new HashMap<>();
                extendedProps.put("bookingCode", booking.getBookingCode());
                extendedProps.put("amount", booking.getTotalAmount() != null ? booking.getTotalAmount().toString() : "0");
                
                // Customer information
                if (booking.getUser() != null) {
                    extendedProps.put("customerName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                    extendedProps.put("customerEmail", booking.getUser().getEmail());
                }
                
                // Vehicle information
                if (booking.getVehicle() != null) {
                    extendedProps.put("vehicleModel", booking.getVehicle().getVehicleModel());
                    extendedProps.put("licensePlate", booking.getVehicle().getLicensePlate());
                }
                
                event.put("extendedProps", extendedProps);
                event.put("borderColor", color);
                
                // Tooltip information
                String tooltip = String.format("%s - %s\nCustomer: %s\nStatus: %s",
                    booking.getBookingCode(),
                    booking.getVehicle() != null ? booking.getVehicle().getVehicleModel() : "Vehicle",
                    booking.getUser() != null ? booking.getUser().getFirstName() + " " + booking.getUser().getLastName() : "Unknown",
                    booking.getStatus().name()
                );
                event.put("tooltip", tooltip);

                events.add(event);
            }

            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    @GetMapping("/management/customer/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomerProfile(@PathVariable String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", "Access denied"));
        }

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

            // Get booking statistics for this customer
            java.util.List<Booking> customerBookings = bookingService.getBookingsByUser(customer);
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