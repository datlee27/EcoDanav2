package com.ecodana.evodanavn1.controller.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/book")
    public String book(@RequestParam String vehicleId, @RequestParam LocalDate pickupDate, @RequestParam LocalDate returnDate, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        // Validate dates
        if (pickupDate.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Pickup date cannot be in the past");
            return "redirect:/vehicles";
        }

        if (returnDate.isBefore(pickupDate)) {
            model.addAttribute("error", "Return date must be after pickup date");
            return "redirect:/vehicles";
        }

        Vehicle vehicle = vehicleService.getVehicleById(vehicleId).orElse(null);
        if (vehicle == null) {
            model.addAttribute("error", "Vehicle not found");
            return "redirect:/vehicles";
        }

        // Check if vehicle is available
        if (vehicle.getStatus() != Vehicle.VehicleStatus.Available) {
            model.addAttribute("error", "Vehicle is not available for booking");
            return "redirect:/vehicles";
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(pickupDate, returnDate) + 1;
        if (days <= 0) {
            model.addAttribute("error", "Booking duration must be at least 1 day");
            return "redirect:/vehicles";
        }

        // Sử dụng VehicleService để lấy giá thuê theo ngày từ JSON
        BigDecimal dailyPrice = vehicle.getDailyPriceFromJson();
        if (dailyPrice == null || dailyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Could not determine the daily rate for this vehicle.");
            return "redirect:/vehicles";
        }
        BigDecimal amount = dailyPrice.multiply(new BigDecimal(days));

        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setPickupDateTime(pickupDate.atStartOfDay());
        booking.setReturnDateTime(returnDate.atStartOfDay());
        booking.setTotalAmount(amount);
        booking.setStatus(Booking.BookingStatus.Pending);
        booking.setBookingCode("BK" + System.currentTimeMillis());
        booking.setRentalType(Booking.RentalType.daily);

        booking.setCreatedDate(java.time.LocalDateTime.now());

        bookingService.addBooking(booking);
        model.addAttribute("booking", booking);
        return "booking-confirmation";
    }
}