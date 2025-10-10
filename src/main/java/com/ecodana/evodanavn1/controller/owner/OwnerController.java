package com.ecodana.evodanavn1.controller.owner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @PostMapping("/cars/{id}")
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
        try {
            vehicleService.getVehicleById(id).ifPresent(vehicle -> {
                vehicle.setStatus(vehicle.getStatus() == Vehicle.VehicleStatus.Available ? Vehicle.VehicleStatus.Unavailable : Vehicle.VehicleStatus.Available);
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle status updated");
            });
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }
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
                    Map<String, Object> prices = new ObjectMapper().readValue(vehicle.getRentalPrices(), new TypeReference<>() {});
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
}