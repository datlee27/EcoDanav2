package com.ecodana.evodanavn1.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ecodana.evodanavn1.dto.BookingDTO;
import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserService;
import com.ecodana.evodanavn1.service.VehicleService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class BookingAdminController {

    private static final Logger logger = LoggerFactory.getLogger(BookingAdminController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    /**
     * Display booking management page
     */
    @GetMapping("/bookings/manage")
    public String bookingsPage(Model model) {
        return "admin/admin-bookings-management";
    }

    /**
     * Get all bookings as JSON
     */
    @GetMapping("/api/bookings")
    @ResponseBody
    public ResponseEntity<List<BookingDTO>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            List<Booking> bookings;
            
            if (status != null && !status.isEmpty() && !"All".equalsIgnoreCase(status)) {
                bookings = bookingService.getBookingsByStatus(status);
            } else {
                bookings = bookingService.getAllBookings();
            }

            // Convert to DTOs with user and vehicle information
            List<BookingDTO> bookingDTOs = bookings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                bookingDTOs = bookingDTOs.stream()
                        .filter(dto -> 
                            (dto.getBookingCode() != null && dto.getBookingCode().toLowerCase().contains(searchLower)) ||
                            (dto.getUserName() != null && dto.getUserName().toLowerCase().contains(searchLower)) ||
                            (dto.getUserEmail() != null && dto.getUserEmail().toLowerCase().contains(searchLower)) ||
                            (dto.getVehicleModel() != null && dto.getVehicleModel().toLowerCase().contains(searchLower)) ||
                            (dto.getLicensePlate() != null && dto.getLicensePlate().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(bookingDTOs);
        } catch (Exception e) {
            logger.error("Error getting all bookings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get booking by ID
     */
    @GetMapping("/api/bookings/{id}")
    @ResponseBody
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
        try {
            return bookingService.findById(id)
                    .map(booking -> ResponseEntity.ok(convertToDTO(booking)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting booking by ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Update booking status
     */
    @PutMapping("/api/bookings/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBookingStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            String reason = request.get("reason");

            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus(newStatus);
            
            if ("Rejected".equalsIgnoreCase(newStatus) || "Cancelled".equalsIgnoreCase(newStatus)) {
                booking.setCancelReason(reason);
            }

            bookingService.updateBooking(booking);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking status updated successfully");
            response.put("booking", convertToDTO(booking));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating booking status for ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating booking status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update booking
     */
    @PutMapping("/api/bookings/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable String id,
            @RequestBody Booking updatedBooking) {
        try {
            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Update fields
            booking.setPickupDateTime(updatedBooking.getPickupDateTime());
            booking.setReturnDateTime(updatedBooking.getReturnDateTime());
            booking.setTotalAmount(updatedBooking.getTotalAmount());
            booking.setStatus(updatedBooking.getStatus());
            booking.setExpectedPaymentMethod(updatedBooking.getExpectedPaymentMethod());
            booking.setRentalType(updatedBooking.getRentalType());
            
            if (updatedBooking.getCancelReason() != null) {
                booking.setCancelReason(updatedBooking.getCancelReason());
            }

            bookingService.updateBooking(booking);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking updated successfully");
            response.put("booking", convertToDTO(booking));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating booking ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating booking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete booking
     */
    @DeleteMapping("/api/bookings/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBooking(@PathVariable String id) {
        try {
            if (!bookingService.findById(id).isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Booking not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            bookingService.deleteBooking(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Booking deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting booking ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting booking: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get booking statistics
     */
    @GetMapping("/api/bookings/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        try {
            Map<String, Object> stats = bookingService.getBookingStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting booking statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Convert Booking entity to DTO with related information
     */
    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setBookingId(booking.getBookingId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setUserId(booking.getUserId());
        dto.setVehicleId(booking.getVehicleId());
        dto.setHandledBy(booking.getHandledBy());
        dto.setPickupDateTime(booking.getPickupDateTime());
        dto.setReturnDateTime(booking.getReturnDateTime());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setStatus(booking.getStatus());
        dto.setDiscountId(booking.getDiscountId());
        dto.setCreatedDate(booking.getCreatedDate());
        dto.setCancelReason(booking.getCancelReason());
        dto.setBookingCode(booking.getBookingCode());
        dto.setExpectedPaymentMethod(booking.getExpectedPaymentMethod());
        dto.setRentalType(booking.getRentalType());
        dto.setTermsAgreed(booking.getTermsAgreed());
        dto.setTermsAgreedAt(booking.getTermsAgreedAt());
        dto.setTermsVersion(booking.getTermsVersion());

        // Get user information
        try {
            logger.debug("Fetching user with ID: {}", booking.getUserId());
            User user = userService.findById(booking.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
                dto.setUserEmail(user.getEmail());
                dto.setUserPhone(user.getPhoneNumber());
                logger.debug("User found: {}", user.getUsername());
            } else {
                logger.warn("User not found for booking {} with userId: {}", booking.getBookingId(), booking.getUserId());
            }
        } catch (Exception e) {
            logger.error("Error fetching user for booking " + booking.getBookingId() + " with userId: " + booking.getUserId(), e);
        }

        // Get vehicle information
        try {
            logger.debug("Fetching vehicle with ID: {}", booking.getVehicleId());
            Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId()).orElse(null);
            if (vehicle != null) {
                dto.setVehicleModel(vehicle.getVehicleModel());
                dto.setLicensePlate(vehicle.getLicensePlate());
                logger.debug("Vehicle found: {}", vehicle.getVehicleModel());
            } else {
                logger.warn("Vehicle not found for booking {} with vehicleId: {}", booking.getBookingId(), booking.getVehicleId());
            }
        } catch (Exception e) {
            logger.error("Error fetching vehicle for booking " + booking.getBookingId() + " with vehicleId: " + booking.getVehicleId(), e);
        }

        // Get handler information
        if (booking.getHandledBy() != null) {
            try {
                User handler = userService.findById(booking.getHandledBy());
                if (handler != null) {
                    dto.setHandledByName(handler.getUsername());
                }
            } catch (Exception e) {
                logger.error("Error fetching handler for booking " + booking.getBookingId(), e);
            }
        }

        return dto;
    }
}
