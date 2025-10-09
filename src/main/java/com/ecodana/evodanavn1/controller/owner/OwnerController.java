package com.ecodana.evodanavn1.controller.owner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    /**
     * Helper method to check if user is authenticated and has owner/staff/admin role
     */
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
            var allVehicles = vehicleService.getAllVehicles();
            var allBookings = bookingService.getAllBookings();

            if (allVehicles == null) allVehicles = new ArrayList<>();
            if (allBookings == null) allBookings = new ArrayList<>();

            model.addAttribute("user", userWithRole);
            model.addAttribute("vehicles", allVehicles);

            try {
                var transmissions = transmissionTypeRepository.findAll();
                var categories = vehicleCategoriesRepository.findAll();

                model.addAttribute("transmissions", transmissions != null ? transmissions : new ArrayList<>());
                model.addAttribute("categories", categories != null ? categories : new ArrayList<>());

                Map<String, String> transmissionMap = new HashMap<>();
                if (transmissions != null) {
                    transmissions.forEach(trans -> transmissionMap.put(trans.getTransmissionTypeId().toString(), trans.getTransmissionTypeName()));
                }

                Map<Integer, String> categoryMap = new HashMap<>();
                if (categories != null) {
                    categories.forEach(cat -> categoryMap.put(cat.getCategoryId(), cat.getCategoryName()));
                }

                model.addAttribute("transmissionMap", transmissionMap);
                model.addAttribute("categoryMap", categoryMap);

            } catch (Exception e) {
                System.out.println("Reference data not available: " + e.getMessage());
                model.addAttribute("transmissions", new ArrayList<>());
                model.addAttribute("categories", new ArrayList<>());
                model.addAttribute("transmissionMap", new HashMap<String, String>());
                model.addAttribute("categoryMap", new HashMap<Integer, String>());
            }

            model.addAttribute("totalVehicles", allVehicles.size());
            model.addAttribute("bookings", allBookings);

            long availableVehiclesCount = allVehicles.stream()
                    .filter(v -> "Available".equals(v.getStatus()))
                    .count();
            model.addAttribute("availableVehicles", availableVehiclesCount);

            try {
                model.addAttribute("activeBookings", bookingService.getActiveBookings());
                model.addAttribute("pendingBookings", bookingService.getPendingBookings());
                model.addAttribute("todayRevenue", bookingService.getTodayRevenue());
            } catch (Exception e) {
                System.out.println("Booking service methods failed: " + e.getMessage());
                model.addAttribute("activeBookings", new ArrayList<>());
                model.addAttribute("pendingBookings", new ArrayList<>());
                model.addAttribute("todayRevenue", 0.0);
            }

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
            vehicle.setVehicleType(carData.get("type"));

            String transmissionTypeId = carData.get("transmissionTypeId");
            if (transmissionTypeId != null && !transmissionTypeId.isEmpty()) {
                vehicle.setTransmissionTypeId(transmissionTypeId);
            }

            String categoryId = carData.get("categoryId");
            if (categoryId != null && !categoryId.isEmpty()) {
                vehicle.setCategoryId(categoryId);
            }

            vehicle.setLicensePlate(carData.get("licensePlate"));

            if (carData.get("yearManufactured") != null && !carData.get("yearManufactured").isEmpty()) {
                vehicle.setYearManufactured(Integer.parseInt(carData.get("yearManufactured")));
            }
            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

            String hourlyRate = carData.getOrDefault("hourlyRate", "0").replaceAll("[^\\d.]", "");
            String dailyRate = carData.getOrDefault("dailyRate", "0").replaceAll("[^\\d.]", "");
            String monthlyRate = carData.getOrDefault("monthlyRate", "0").replaceAll("[^\\d.]", "");

            String rentalPricesJson = String.format(
                    "{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}",
                    hourlyRate, dailyRate, monthlyRate
            );
            vehicle.setRentalPrices(rentalPricesJson);

            if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
            }

            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
            vehicle.setStatus(carData.getOrDefault("status", "Available"));
            vehicle.setCreatedDate(java.time.LocalDateTime.now());

            if (images != null && images.length > 0 && images[0] != null && !images[0].isEmpty() && cloudName != null && !cloudName.isBlank()) {
                try {
                    com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(
                            java.util.Map.of(
                                    "cloud_name", cloudName,
                                    "api_key", cloudApiKey,
                                    "api_secret", cloudApiSecret
                            )
                    );
                    java.util.Map uploadResult = cloudinary.uploader().upload(images[0].getBytes(), java.util.Map.of("folder", "ecodana/vehicles"));
                    Object url = uploadResult.get("secure_url");
                    if (url != null) {
                        vehicle.setImageUrl(url.toString());
                    }
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                }
            }

            vehicleService.saveVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle added successfully!");

        } catch (Exception e) {
            System.err.println("Error saving vehicle: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add vehicle: " + e.getMessage());
        }

        return "redirect:/owner/dashboard?section=cars";
    }

    @PutMapping("/cars/{id}")
    public String updateCar(@PathVariable String id,
                            @RequestParam Map<String, String> carData,
                            @RequestParam(value = "images", required = false) MultipartFile[] images,
                            HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        try {
            var vehicleOpt = vehicleService.getVehicleById(id);
            if (vehicleOpt.isPresent()) {
                var vehicle = vehicleOpt.get();
                vehicle.setVehicleModel(carData.get("model"));
                vehicle.setVehicleType(carData.get("type"));

                if (carData.containsKey("transmissionTypeId") && carData.get("transmissionTypeId") != null && !carData.get("transmissionTypeId").isBlank()) {
                    vehicle.setTransmissionTypeId(carData.get("transmissionTypeId"));
                }
                if (carData.containsKey("categoryId") && carData.get("categoryId") != null && !carData.get("categoryId").isBlank()) {
                    vehicle.setCategoryId(carData.get("categoryId"));
                }
                vehicle.setLicensePlate(carData.get("licensePlate"));
                if (carData.get("yearManufactured") != null && !carData.get("yearManufactured").isBlank()) {
                    try {
                        vehicle.setYearManufactured(Integer.parseInt(carData.get("yearManufactured")));
                    } catch (NumberFormatException ignored) {}
                }
                vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
                vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

                String hourlyRate = carData.getOrDefault("hourlyRate", "0").replaceAll("[^\\d.]", "");
                String dailyRate = carData.getOrDefault("dailyRate", "0").replaceAll("[^\\d.]", "");
                String monthlyRate = carData.getOrDefault("monthlyRate", "0").replaceAll("[^\\d.]", "");

                String rentalPricesJson = String.format(
                        "{\"hourly\": %s, \"daily\": %s, \"monthly\": %s}",
                        hourlyRate, dailyRate, monthlyRate
                );
                vehicle.setRentalPrices(rentalPricesJson);

                if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                    vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
                }

                vehicle.setDescription(carData.get("description"));
                vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
                if (carData.containsKey("status")) {
                    vehicle.setStatus(carData.get("status"));
                }

                if (images != null && images.length > 0 && images[0] != null && !images[0].isEmpty() && cloudName != null && !cloudName.isBlank()) {
                    try {
                        com.cloudinary.Cloudinary cloudinary = new com.cloudinary.Cloudinary(
                                java.util.Map.of(
                                        "cloud_name", cloudName,
                                        "api_key", cloudApiKey,
                                        "api_secret", cloudApiSecret
                                )
                        );
                        java.util.Map uploadResult = cloudinary.uploader().upload(images[0].getBytes(), java.util.Map.of("folder", "ecodana/vehicles"));
                        Object url = uploadResult.get("secure_url");
                        if (url != null) {
                            vehicle.setImageUrl(url.toString());
                        }
                    } catch (Exception ex) {
                        redirectAttributes.addFlashAttribute("error", "Image upload failed: " + ex.getMessage());
                    }
                }

                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
            }
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
            var vehicleOpt = vehicleService.getVehicleById(id);
            if (vehicleOpt.isPresent()) {
                var vehicle = vehicleOpt.get();
                String currentStatus = vehicle.getStatus();
                if ("Available".equals(currentStatus)) {
                    vehicle.setStatus("Unavailable");
                } else {
                    vehicle.setStatus("Available");
                }
                vehicleService.updateVehicle(vehicle);
                redirectAttributes.addFlashAttribute("success", "Vehicle status updated");
            } else {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }

        return "redirect:/owner/dashboard?section=cars";
    }

    @DeleteMapping("/cars/{id}")
    @ResponseBody
    public Map<String, Object> deleteCar(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }

        try {
            vehicleService.deleteVehicle(id);
            response.put("status", "success");
            response.put("message", "Vehicle deleted successfully");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete vehicle: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/bookings/{id}/accept")
    @ResponseBody
    public Map<String, Object> acceptBooking(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }

        try {
            var bookingOpt = bookingService.findById(id);
            if (bookingOpt.isPresent()) {
                var booking = bookingOpt.get();
                booking.setStatus("Confirmed");
                bookingService.updateBooking(booking);
                response.put("status", "success");
                response.put("message", "Booking accepted successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to accept booking: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/bookings/{id}/decline")
    @ResponseBody
    public Map<String, Object> declineBooking(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || (!userService.isAdmin(currentUser) && !userService.isStaff(currentUser))) {
            response.put("status", "error");
            response.put("message", "Access denied");
            return response;
        }

        try {
            var bookingOpt = bookingService.findById(id);
            if (bookingOpt.isPresent()) {
                var booking = bookingOpt.get();
                booking.setStatus("Cancelled");
                bookingService.updateBooking(booking);
                response.put("status", "success");
                response.put("message", "Booking declined successfully");
            } else {
                response.put("status", "error");
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to decline booking: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam Map<String, String> ownerUpdate,
                                HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        try {
            User currentUser = (User) session.getAttribute("currentUser");
            User userWithRole = userService.getUserWithRole(currentUser.getEmail());

            if (userWithRole != null) {
                userWithRole.setFirstName(ownerUpdate.get("firstName"));
                userWithRole.setLastName(ownerUpdate.get("lastName"));
                userWithRole.setPhoneNumber(ownerUpdate.get("phoneNumber"));

                userService.updateUser(userWithRole);
                session.setAttribute("currentUser", userWithRole);
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
    public Map<String, Object> getCarForEdit(@PathVariable String id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            var vehicleOpt = vehicleService.getVehicleById(id);
            if (vehicleOpt.isPresent()) {
                var vehicle = vehicleOpt.get();
                Map<String, Object> car = new HashMap<>();
                car.put("id", vehicle.getVehicleId());
                car.put("model", vehicle.getVehicleModel());
                car.put("type", vehicle.getVehicleType());
                car.put("transmissionTypeId", vehicle.getTransmissionTypeId());
                car.put("categoryId", vehicle.getCategoryId());
                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());
                car.put("dailyRate", vehicle.getPricePerDay());
                car.put("hourlyRate", vehicle.getPricePerHour());
                car.put("monthlyRate", vehicle.getPricePerMonth());
                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());
                car.put("status", vehicle.getStatus());
                car.put("yearManufactured", vehicle.getYearManufactured());
                response.put("status", "success");
                response.put("data", car);
            } else {
                response.put("status", "error");
                response.put("message", "Vehicle not found");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get vehicle: " + e.getMessage());
        }

        return response;
    }
}