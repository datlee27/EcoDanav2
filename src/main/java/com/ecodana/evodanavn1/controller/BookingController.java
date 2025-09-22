package com.ecodana.evodanavn1.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
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
        
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId).orElse(null);
        if (vehicle != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(pickupDate, returnDate);
            BigDecimal amount = vehicle.getPricePerDay().multiply(new BigDecimal(days));
            
            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID().toString());
            booking.setUserId(user.getId());
            booking.setVehicleId(vehicle.getVehicleId());
            booking.setPickupDateTime(pickupDate.atStartOfDay());
            booking.setReturnDateTime(returnDate.atStartOfDay());
            booking.setTotalAmount(amount);
            booking.setStatus("Pending");
            booking.setBookingCode("BK" + System.currentTimeMillis());
            booking.setRentalType("daily");
            booking.setCustomerName(user.getFirstName() + " " + user.getLastName());
            booking.setCustomerEmail(user.getEmail());
            booking.setCustomerPhone(user.getPhoneNumber());
            
            bookingService.addBooking(booking);
            model.addAttribute("booking", booking);
            return "booking-confirmation";
        }
        return "redirect:/vehicles";
    }
}