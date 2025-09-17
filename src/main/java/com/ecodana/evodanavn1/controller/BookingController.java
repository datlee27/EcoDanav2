package com.ecodana.evodanavn1.controller;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public String book(@RequestParam String vehicleId, @RequestParam LocalDate pickupDate, @RequestParam LocalDate returnDate, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        Vehicle vehicle = // Get from service by id
                new VehicleService().getVehicleById(vehicleId).orElse(null);
        if (vehicle != null) {
            double amount = vehicle.getPrice() * java.time.temporal.ChronoUnit.DAYS.between(pickupDate, returnDate);
            Booking booking = new Booking("new", user, vehicle, pickupDate, returnDate, amount, "Pending");
            bookingService.addBooking(booking);
            model.addAttribute("booking", booking);
            return "booking-confirmation";
        }
        return "redirect:/vehicles";
    }
}