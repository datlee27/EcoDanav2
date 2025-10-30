package com.ecodana.evodanavn1.controller.owner;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.repository.TransmissionTypeRepository;
import com.ecodana.evodanavn1.repository.VehicleCategoriesRepository;
import com.ecodana.evodanavn1.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner")
public class OwnerController {
    private static final Logger logger = LoggerFactory.getLogger(OwnerController.class);
    private final UserService userService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final TransmissionTypeRepository transmissionTypeRepository;
    private final VehicleCategoriesRepository vehicleCategoriesRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoleService roleService;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud_name:}")
    private String cloudName;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_key:}")
    private String cloudApiKey;

    @org.springframework.beans.factory.annotation.Value("${cloudinary.api_secret:}")
    private String cloudApiSecret;

    @Autowired
    public OwnerController(UserService userService, VehicleService vehicleService, BookingService bookingService, TransmissionTypeRepository transmissionTypeRepository, VehicleCategoriesRepository vehicleCategoriesRepository,NotificationService notificationService,
                           RoleService roleService) {
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.transmissionTypeRepository = transmissionTypeRepository;
        this.vehicleCategoriesRepository = vehicleCategoriesRepository;
        this.notificationService = notificationService;
        this.roleService = roleService;
    }

    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        if (!userService.isOwner(currentUser) && !userService.isAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Access denied. Owner role required.");
            return "redirect:/login";
        }
        return null;
    }

    private String checkAuthenticated(HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access this page.");
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        return null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("totalVehicles", vehicleService.getAllVehicles().size());
        model.addAttribute("availableVehicles", vehicleService.getAvailableVehicles().size());

        long pendingBookingsCount = bookingService.getPendingBookings().size();
        model.addAttribute("pendingBookings", pendingBookingsCount);

        long rentedVehiclesCount = vehicleService.getAllVehicles().stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.Rented)
                .count();
        model.addAttribute("rentedVehicles", rentedVehiclesCount);


        return "owner/dashboard";
    }

    @GetMapping("/cars")
    public String carsPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        User currentUser = (User) session.getAttribute("currentUser");

        model.addAttribute("currentPage", "cars");

        List<Vehicle> allVehicles = vehicleService.getVehiclesByOwnerId(currentUser.getId());
        model.addAttribute("vehicles", allVehicles);

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

        model.addAttribute("availableCount", availableCount);
        model.addAttribute("rentedCount", rentedCount);
        model.addAttribute("maintenanceCount", maintenanceCount);
        model.addAttribute("unavailableCount", unavailableCount);
        model.addAttribute("newVehicle", new Vehicle());
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

        User currentUser = (User) session.getAttribute("currentUser");

        model.addAttribute("currentPage", "bookings");

        List<Booking> allBookings = bookingService.getBookingsByOwnerId(currentUser.getId());
        model.addAttribute("bookings", allBookings);

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
        return "owner/profile-management";
    }

    @GetMapping("/cars/add")
    public String showAddCarForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redirect = checkAuthenticated(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        model.addAttribute("transmissions", transmissionTypeRepository.findAll());
        model.addAttribute("categories", vehicleCategoriesRepository.findAll());

        if (!model.containsAttribute("vehicle")) {
            model.addAttribute("vehicle", new Vehicle());
        }

        return "owner/vehicle-add";
    }

    @PostMapping("/cars")
    public String addCar(
            // Dùng @ModelAttribute để binding và validation
            @ModelAttribute("vehicle") Vehicle vehicle,
            // Giữ lại @RequestParam cho các trường không có trong model
            @RequestParam Map<String, String> carData,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImageFile,
            @RequestParam(value = "auxiliaryImages", required = false) MultipartFile[] auxiliaryImageFiles,
            // Input ẩn để xác định nguồn gốc
            @RequestParam("_sourceView") String sourceView,
            HttpSession session, RedirectAttributes redirectAttributes, Model model) {

        // 1. Kiểm tra đăng nhập
        String redirect = checkAuthenticated(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        // 2. Chuẩn bị đường dẫn trả về khi lỗi
        // Nếu sourceView là "redirect:/owner/cars", chúng ta cần thêm flash attributes
        // Nếu sourceView là "owner/vehicle-add", chúng ta cần thêm model attributes
        String errorRedirectPath = sourceView.startsWith("redirect:") ? sourceView : "owner/vehicle-add";

        // 3. Lấy User
        User currentUser = (User) session.getAttribute("currentUser");

        try {
            // --- VALIDATION (Backend) ---
            if (carData.get("model") == null || carData.get("model").isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập Tên xe.");
            }
            vehicle.setVehicleModel(carData.get("model")); // Gán lại vào model

            String licensePlate = carData.get("licensePlate");
            if (licensePlate == null || licensePlate.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập Biển số xe.");
            }
            if (vehicleService.vehicleExistsByLicensePlate(licensePlate)) {
                throw new IllegalArgumentException("Biển số xe này đã tồn tại.");
            }
            vehicle.setLicensePlate(licensePlate);

            String dailyRateStr = carData.get("dailyRate");
            if (dailyRateStr == null || dailyRateStr.isBlank() || new BigDecimal(dailyRateStr).compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Vui lòng nhập Giá theo ngày (phải lớn hơn 0).");
            }

            if (mainImageFile == null || mainImageFile.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng tải lên Ảnh chính.");
            }
            // --- KẾT THÚC VALIDATION ---


            // --- BẮT ĐẦU XỬ LÝ DỮ LIỆU (Tương tự code cũ của bạn) ---

            vehicle.setVehicleId(java.util.UUID.randomUUID().toString());
            vehicle.setVehicleType(Vehicle.VehicleType.valueOf(carData.get("type")));

            if (carData.get("transmissionTypeId") != null && !carData.get("transmissionTypeId").isEmpty()) {
                transmissionTypeRepository.findById(Integer.parseInt(carData.get("transmissionTypeId"))).ifPresent(vehicle::setTransmissionType);
            }
            if (carData.get("categoryId") != null && !carData.get("categoryId").isEmpty()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(carData.get("categoryId"))).ifPresent(vehicle::setCategory);
            }

            if (carData.get("yearManufactured") != null && !carData.get("yearManufactured").isEmpty()) {
                vehicle.setYearManufactured(Integer.parseInt(carData.get("yearManufactured")));
            }
            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

            BigDecimal hourlyRate = carData.containsKey("hourlyRate") && !carData.get("hourlyRate").isEmpty() ? new BigDecimal(carData.get("hourlyRate")) : BigDecimal.ZERO;
            BigDecimal dailyRate = new BigDecimal(dailyRateStr);
            BigDecimal monthlyRate = carData.containsKey("monthlyRate") && !carData.get("monthlyRate").isEmpty() ? new BigDecimal(carData.get("monthlyRate")) : BigDecimal.ZERO;

            Map<String, BigDecimal> pricesMap = new HashMap<>();
            pricesMap.put("hourly", hourlyRate);
            pricesMap.put("daily", dailyRate);
            pricesMap.put("monthly", monthlyRate);
            vehicle.setRentalPrices(objectMapper.writeValueAsString(pricesMap));

            if (carData.get("batteryCapacity") != null && !carData.get("batteryCapacity").isEmpty()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(carData.get("batteryCapacity")));
            }
            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));
            vehicle.setStatus(Vehicle.VehicleStatus.PendingApproval); // Luôn chờ duyệt
            vehicle.setCreatedDate(java.time.LocalDateTime.now());

            vehicle.setOwnerId(currentUser.getId());
            vehicle.setLastUpdatedBy(currentUser);

            if (carData.containsKey("features")) {
                vehicle.setFeatures(carData.get("features"));
            } else {
                vehicle.setFeatures("[]");
            }

            // --- Xử lý Cloudinary ---
            Cloudinary cloudinary = null;
            if (cloudName != null && !cloudName.isBlank() && cloudApiKey != null && !cloudApiKey.isBlank() && cloudApiSecret != null && !cloudApiSecret.isBlank()) {
                cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", cloudName,
                        "api_key", cloudApiKey,
                        "api_secret", cloudApiSecret));
            } else {
                logger.warn("Cloudinary credentials not fully configured. Images will not be uploaded.");
            }

            // Upload ảnh chính
            if (cloudinary != null) {
                try {
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(mainImageFile.getBytes(), ObjectUtils.asMap("folder", "ecodana/vehicles"));
                    vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Lỗi tải lên Ảnh chính: " + ex.getMessage());
                }
            } else if (!mainImageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Chưa cấu hình Cloudinary, ảnh chính không được tải lên.");
            }

            // Upload ảnh phụ
            List<String> auxiliaryImageUrls = new ArrayList<>();
            if (cloudinary != null && auxiliaryImageFiles != null && auxiliaryImageFiles.length > 0) {
                if (auxiliaryImageFiles.length > 10) {
                    throw new IllegalArgumentException("Chỉ được phép tải lên tối đa 10 ảnh phụ.");
                }
                int uploadedCount = 0;
                for (MultipartFile file : auxiliaryImageFiles) {
                    if (uploadedCount >= 10) break;
                    if (!file.isEmpty()) {
                        try {
                            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "ecodana/vehicles/auxiliary"));
                            auxiliaryImageUrls.add(uploadResult.get("secure_url").toString());
                            uploadedCount++;
                        } catch (Exception ex) {
                            redirectAttributes.addFlashAttribute("warning", "Lỗi tải lên ảnh phụ " + file.getOriginalFilename() + ": " + ex.getMessage());
                        }
                    }
                }
            }
            vehicle.setImageUrls(auxiliaryImageUrls.isEmpty() ? "[]" : objectMapper.writeValueAsString(auxiliaryImageUrls));

            // --- Lưu vào DB ---
            vehicleService.saveVehicle(vehicle);

            // Gửi thông báo cho Admin
            try {
                notificationService.createNotificationForAllAdmins(
                        "Xe mới " + vehicle.getVehicleModel() + " (chủ xe: " + currentUser.getUsername() + ") đang chờ duyệt.",
                        vehicle.getVehicleId(),
                        "VEHICLE_APPROVAL"
                );
            } catch (Exception e) {
                logger.error("Failed to create admin notification for new vehicle.", e);
            }

            // === XỬ LÝ THÀNH CÔNG ===
            redirectAttributes.addFlashAttribute("success", "Đăng ký xe thành công! Xe của bạn đang chờ Admin duyệt.");

            // Nếu thành công, luôn về trang quản lý xe
            return "redirect:/";

        } catch (IllegalArgumentException e) { // Lỗi validation
            logger.warn("Validation failed for addCar: {}", e.getMessage());
            // Trả lỗi về đúng form
            if (sourceView.startsWith("redirect:")) {
                // Gửi lỗi về modal (trang /owner/cars)
                redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
                // Thêm param để JS tự mở modal
                redirectAttributes.addAttribute("openModal", "add-car-modal");
            } else {
                // Gửi lỗi về trang đầy đủ (owner/vehicle-add)
                model.addAttribute("error", "Lỗi: " + e.getMessage());
                // Gửi lại các dropdown
                model.addAttribute("transmissions", transmissionTypeRepository.findAll());
                model.addAttribute("categories", vehicleCategoriesRepository.findAll());
                // Giữ lại các giá trị đã nhập
                model.addAttribute("vehicle", vehicle);
            }
            return errorRedirectPath; // Trả về view "owner/vehicle-add" hoặc "redirect:/owner/cars"

        } catch (Exception e) { // Lỗi hệ thống
            logger.error("Error saving vehicle", e);
            // Trả lỗi về đúng form
            if (sourceView.startsWith("redirect:")) {
                redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống khi thêm xe: " + e.getMessage());
            } else {
                model.addAttribute("error", "Lỗi hệ thống khi thêm xe: " + e.getMessage());
                model.addAttribute("transmissions", transmissionTypeRepository.findAll());
                model.addAttribute("categories", vehicleCategoriesRepository.findAll());
                model.addAttribute("vehicle", vehicle);
            }
            return errorRedirectPath;
        }
    }

    @PostMapping("/cars/{id}")
    public String updateCar(@PathVariable String id, @RequestParam Map<String, String> carData,
                            @RequestParam(value = "mainImage", required = false) MultipartFile mainImageFile,
                            @RequestParam(value = "auxiliaryImages", required = false) MultipartFile[] auxiliaryImageFiles,
                            HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        String redirect = checkAuthentication(session, redirectAttributes, model);
        if (redirect != null) return redirect;

        Cloudinary cloudinary = null;
        if (cloudName != null && !cloudName.isBlank() && cloudApiKey != null && !cloudApiKey.isBlank() && cloudApiSecret != null && !cloudApiSecret.isBlank()) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", cloudApiKey,
                    "api_secret", cloudApiSecret));
        } else {
            redirectAttributes.addFlashAttribute("warning", "Cloudinary credentials not fully configured. Images may not be uploaded.");
        }

        try {
            java.util.Optional<Vehicle> vehicleOptional = vehicleService.getVehicleById(id);
            if (vehicleOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found.");
                return "redirect:/owner/cars";
            }

            Vehicle vehicle = vehicleOptional.get();
            // ... (Cập nhật các thuộc tính) ...
            vehicle.setVehicleModel(carData.get("model"));
            vehicle.setVehicleType(Vehicle.VehicleType.valueOf(carData.get("type")));

            String transmissionTypeId = carData.get("transmissionTypeId");
            if (transmissionTypeId != null && !transmissionTypeId.isBlank()) {
                transmissionTypeRepository.findById(Integer.parseInt(transmissionTypeId)).ifPresent(vehicle::setTransmissionType);
            } else {
                vehicle.setTransmissionType(null);
            }

            String categoryId = carData.get("categoryId");
            if (categoryId != null && !categoryId.isBlank()) {
                vehicleCategoriesRepository.findById(Integer.parseInt(categoryId)).ifPresent(vehicle::setCategory);
            } else {
                vehicle.setCategory(null);
            }

            vehicle.setLicensePlate(carData.get("licensePlate"));

            String yearManufactured = carData.get("yearManufactured");
            if (yearManufactured != null && !yearManufactured.isBlank()) {
                vehicle.setYearManufactured(Integer.parseInt(yearManufactured));
            }

            vehicle.setSeats(Integer.parseInt(carData.getOrDefault("seats", "4")));
            vehicle.setOdometer(Integer.parseInt(carData.getOrDefault("odometer", "0")));

            BigDecimal hourlyRate = carData.containsKey("hourlyRate") && !carData.get("hourlyRate").isEmpty() ? new BigDecimal(carData.get("hourlyRate")) : BigDecimal.ZERO;
            BigDecimal dailyRate = carData.containsKey("dailyRate") && !carData.get("dailyRate").isEmpty() ? new BigDecimal(carData.get("dailyRate")) : BigDecimal.ZERO;
            BigDecimal monthlyRate = carData.containsKey("monthlyRate") && !carData.get("monthlyRate").isEmpty() ? new BigDecimal(carData.get("monthlyRate")) : BigDecimal.ZERO;

            Map<String, BigDecimal> pricesMap = new HashMap<>();
            pricesMap.put("hourly", hourlyRate);
            pricesMap.put("daily", dailyRate);
            pricesMap.put("monthly", monthlyRate);
            vehicle.setRentalPrices(objectMapper.writeValueAsString(pricesMap));


            String batteryCapacity = carData.get("batteryCapacity");
            if (batteryCapacity != null && !batteryCapacity.isBlank()) {
                vehicle.setBatteryCapacity(new java.math.BigDecimal(batteryCapacity));
            }

            vehicle.setDescription(carData.get("description"));
            vehicle.setRequiresLicense(Boolean.parseBoolean(carData.getOrDefault("requiresLicense", "true")));

            if (carData.containsKey("status")) {
                vehicle.setStatus(Vehicle.VehicleStatus.valueOf(carData.get("status")));
            }
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                vehicle.setLastUpdatedBy(currentUser);
            }

            // ===================================
            // === BẮT ĐẦU SỬA LỖI FEATURES ===
            // ===================================
            // Lấy chuỗi JSON features từ hidden input (do JS tạo ra)
            if (carData.containsKey("features")) {
                vehicle.setFeatures(carData.get("features"));
            } else {
                vehicle.setFeatures("[]"); // Mặc định là mảng rỗng
            }
            // ===================================
            // === KẾT THÚC SỬA LỖI FEATURES ===
            // ===================================

            // Xử lý ảnh chính MỚI (nếu có)
            if (cloudinary != null && mainImageFile != null && !mainImageFile.isEmpty()) {
                try {
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(mainImageFile.getBytes(), ObjectUtils.asMap("folder", "ecodana/vehicles"));
                    vehicle.setMainImageUrl(uploadResult.get("secure_url").toString());
                } catch (Exception ex) {
                    redirectAttributes.addFlashAttribute("error", "Main image upload failed: " + ex.getMessage());
                }
            } else if (mainImageFile != null && !mainImageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Main image provided but Cloudinary not configured.");
            }

            // Xử lý ảnh phụ MỚI (nếu có) - Sẽ thay thế toàn bộ list cũ
            if (cloudinary != null && auxiliaryImageFiles != null && auxiliaryImageFiles.length > 0 && !auxiliaryImageFiles[0].isEmpty()) {
                List<String> auxiliaryImageUrls = new ArrayList<>();
                if (auxiliaryImageFiles.length > 10) {
                    redirectAttributes.addFlashAttribute("warning", "Maximum 10 auxiliary images allowed. Only the first 10 were uploaded.");
                }
                int uploadedCount = 0;
                for (MultipartFile file : auxiliaryImageFiles) {
                    if (uploadedCount >= 10) break;
                    if (!file.isEmpty()) {
                        try {
                            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "ecodana/vehicles/auxiliary"));
                            auxiliaryImageUrls.add(uploadResult.get("secure_url").toString());
                            uploadedCount++;
                        } catch (Exception ex) {
                            redirectAttributes.addFlashAttribute("error", "Auxiliary image upload failed for file " + file.getOriginalFilename() + ": " + ex.getMessage());
                        }
                    }
                }
                if (!auxiliaryImageUrls.isEmpty()) {
                    vehicle.setImageUrls(objectMapper.writeValueAsString(auxiliaryImageUrls));
                }
            } else if (auxiliaryImageFiles != null && auxiliaryImageFiles.length > 0 && !auxiliaryImageFiles[0].isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Auxiliary images provided but Cloudinary not configured.");
            }

            vehicleService.updateVehicle(vehicle);
            redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");

        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update vehicle: Invalid number format provided.");
        } catch (Exception e) {
            e.printStackTrace();
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