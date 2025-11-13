package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Vehicle; // Thêm import này
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.VehicleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList; // Thêm import này
import java.util.HashMap;
import java.util.List; // Thêm import này
import java.util.Map;

@RestController
@RequestMapping("/owner/api")
public class OwnerApiController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private BookingService bookingService;

    private static final Logger logger = LoggerFactory.getLogger(OwnerApiController.class); // Thêm Logger

    // Thêm ObjectMapper để parse JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/cars/{id}")
    public ResponseEntity<?> getCarForEdit(@PathVariable String id) {
        try {
            // Sử dụng getVehicleById để lấy đối tượng Vehicle đầy đủ
            return vehicleService.getVehicleById(id).map(vehicle -> {
                Map<String, Object> car = new HashMap<>();
                car.put("vehicleId", vehicle.getVehicleId());
                car.put("vehicleModel", vehicle.getVehicleModel());

                if (vehicle.getVehicleType() != null) {
                    car.put("type", vehicle.getVehicleType().name());
                }

                // --- SỬA ĐỔI: Tạo object lồng nhau cho JS ---
                if (vehicle.getTransmissionType() != null) {
                    car.put("transmission", Map.of("transmissionTypeId", vehicle.getTransmissionType().getTransmissionTypeId()));
                } else {
                    car.put("transmission", null); // Gửi null nếu không có
                }

                // --- SỬA ĐỔI: Tạo object lồng nhau cho JS ---
                if (vehicle.getCategory() != null) {
                    car.put("category", Map.of("categoryId", vehicle.getCategory().getCategoryId()));
                } else {
                    car.put("category", null); // Gửi null nếu không có
                }

                car.put("licensePlate", vehicle.getLicensePlate());
                car.put("seats", vehicle.getSeats());
                car.put("odometer", vehicle.getOdometer());

                // --- SỬA ĐỔI: Sử dụng hàm get...FromJson() ---
                try {
                    // Sử dụng các hàm transient của Vehicle.java để lấy giá đã parse
                    car.put("hourlyRate", vehicle.getHourlyPriceFromJson());
                    car.put("dailyRate", vehicle.getDailyPriceFromJson());
                    car.put("monthlyRate", vehicle.getMonthlyPriceFromJson());
                } catch (Exception e) {
                    car.put("hourlyRate", 0);
                    car.put("dailyRate", 0);
                    car.put("monthlyRate", 0);
                }

                car.put("batteryCapacity", vehicle.getBatteryCapacity());
                car.put("description", vehicle.getDescription());
                car.put("requiresLicense", vehicle.getRequiresLicense());

                if(vehicle.getStatus() != null) {
                    car.put("status", vehicle.getStatus().name());
                }

                car.put("yearManufactured", vehicle.getYearManufactured());

                // --- BỔ SUNG CÁC TRƯỜNG CÒN THIẾU ---

                // 1. Thêm Ảnh chính
                car.put("mainImageUrl", vehicle.getMainImageUrl());

                // 2. Thêm Ảnh phụ (dùng hàm transient đã parse sẵn)
                car.put("imageUrls", vehicle.getImageUrlsFromJson());

                // 3. Thêm Tính năng (dùng hàm transient đã parse sẵn)
                car.put("features", vehicle.getFeaturesFromJson());

                // --- KẾT THÚC BỔ SUNG ---

                return ResponseEntity.ok(car);
            }).orElse(ResponseEntity.status(404).body(Map.of("message", "Vehicle not found")));
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console server để debug
            return ResponseEntity.status(500).body(Map.of("message", "Failed to get vehicle: " + e.getMessage()));
        }
    }

    /**
     * Endpoint cuối cùng, xử lý việc hoàn tất chuyến đi,
     * nhận ghi chú, ảnh trả xe và trạng thái cuối cùng.
     */
    @PostMapping("/bookings/complete-trip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeTrip(
            @RequestParam("bookingId") String bookingId,
            @RequestParam(value = "returnNotes", required = false) String returnNotes,
            @RequestParam("newStatus") String newStatus, // "Completed" hoặc "Maintenance"
            @RequestParam(value = "returnImages", required = false) List<MultipartFile> returnImages) {

        try {
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));

            // 1. Lưu ghi chú trả xe
            if (returnNotes != null && !returnNotes.isEmpty()) {
                booking.setReturnNotes(returnNotes);
            }

            // 2. Upload và lưu link ảnh
            if (returnImages != null && !returnImages.isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (MultipartFile image : returnImages) {
                    // == LOGIC UPLOAD ẢNH CỦA BẠN ==
                    // Ví dụ: String url = cloudinaryService.uploadFile(image);
                    // imageUrls.add(url);
                }
                // Lưu danh sách URL vào booking
                booking.setReturnImageUrls(imageUrls);
                logger.info("Đã lưu {} ảnh trả xe cho booking {}", imageUrls.size(), bookingId);
            }

            // 3. Cập nhật trạng thái cuối cùng
            // Luôn đặt trạng thái booking là Completed
            booking.setStatus(Booking.BookingStatus.Completed);

            if ("Maintenance".equalsIgnoreCase(newStatus)) {
                // Nếu chủ xe chọn "Xác nhận và Bảo trì"
                Vehicle vehicle = booking.getVehicle();
                if (vehicle != null) {
                    // Giả sử bạn có trạng thái MAINTENANCE trong enum VehicleStatus
                    // vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
                    // vehicleService.updateVehicle(vehicle); // (Hoặc .save(vehicle))
                    logger.info("Đã cập nhật trạng thái xe {} thành Bảo trì.", vehicle.getVehicleId());
                } else {
                    logger.warn("Không tìm thấy xe cho booking {} để cập nhật bảo trì.", bookingId);
                }
            }

            bookingService.updateBooking(booking); // Lưu tất cả thay đổi vào booking

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã hoàn tất chuyến đi thành công");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Lỗi khi hoàn tất chuyến đi (ID: " + bookingId + "): ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}