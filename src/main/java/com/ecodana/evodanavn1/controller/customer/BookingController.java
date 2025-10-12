package com.ecodana.evodanavn1.controller.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.ecodana.evodanavn1.dto.BookingRequest;
import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecodana.evodanavn1.model.Booking;

import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.VehicleService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    /**
     * Create new booking
     */
    @PostMapping("/create")
    public String createBooking(@ModelAttribute BookingRequest bookingRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt xe!");
            return "redirect:/login";
        }

        try {
            // Parse dates and times
            LocalDate pickup = LocalDate.parse(bookingRequest.getPickupDate());
            LocalDate returnD = LocalDate.parse(bookingRequest.getReturnDate());
            LocalTime pickupT = LocalTime.parse(bookingRequest.getPickupTime());
            LocalTime returnT = LocalTime.parse(bookingRequest.getReturnTime());
            
            LocalDateTime pickupDateTime = LocalDateTime.of(pickup, pickupT);
            LocalDateTime returnDateTime = LocalDateTime.of(returnD, returnT);

            // Validate dates
            if (pickupDateTime.isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("error", "Ngày nhận xe không thể là quá khứ!");
                return "redirect:/vehicles/" + bookingRequest.getVehicleId();
            }

            if (returnDateTime.isBefore(pickupDateTime)) {
                redirectAttributes.addFlashAttribute("error", "Ngày trả xe phải sau ngày nhận xe!");
                return "redirect:/vehicles/" + bookingRequest.getVehicleId();
            }

            // Get vehicle
            Vehicle vehicle = vehicleService.getVehicleById(bookingRequest.getVehicleId()).orElse(null);
            if (vehicle == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy xe!");
                return "redirect:/vehicles";
            }

            // Check if vehicle is available
            if (!"Available".equals(vehicle.getStatus().toString())) {
                redirectAttributes.addFlashAttribute("error", "Xe không khả dụng để đặt!");
                return "redirect:/vehicles/" + bookingRequest.getVehicleId();
            }

            // Create booking
            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID().toString());
            booking.setUser(user);
            booking.setVehicle(vehicle);
            booking.setPickupDateTime(pickupDateTime);
            booking.setReturnDateTime(returnDateTime);
            booking.setTotalAmount(bookingRequest.getTotalAmount());
            booking.setStatus(Booking.BookingStatus.Pending);
            booking.setBookingCode("BK" + System.currentTimeMillis());
            booking.setRentalType(Booking.RentalType.daily);
            booking.setCreatedDate(LocalDateTime.now());
            booking.setTermsAgreed(true);
            booking.setTermsAgreedAt(LocalDateTime.now());
            booking.setExpectedPaymentMethod("Cash");

            bookingService.addBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", "Đặt xe thành công!");
            redirectAttributes.addFlashAttribute("bookingCode", booking.getBookingCode());
            
            return "redirect:/booking/confirmation/" + booking.getBookingId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vehicles/" + bookingRequest.getVehicleId();
        }
    }

    /**
     * Show booking confirmation page
     */
    @GetMapping("/confirmation/{bookingId}")
    public String bookingConfirmation(@PathVariable String bookingId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null) {
            return "redirect:/booking/my-bookings";
        }

        // Get user ID from booking entity relationship
        String bookingUserId = booking.getUser() != null ? booking.getUser().getId() : null;
        if (bookingUserId == null || !bookingUserId.equals(user.getId())) {
            return "redirect:/booking/my-bookings";
        }

        // Get vehicle from booking entity relationship
        Vehicle vehicle = booking.getVehicle();
        if (vehicle == null) {
            return "redirect:/booking/my-bookings";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("currentUser", user);
        
        return "customer/booking-confirmation";
    }

    /**
     * Show user's bookings
     */
    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getBookingsByUser(user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentUser", user);
        
        return "customer/my-bookings";
    }

    /**
     * Cancel booking
     */
    @PostMapping("/cancel/{bookingId}")
    public String cancelBooking(
            @PathVariable String bookingId,
            @RequestParam(required = false) String cancelReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy booking!");
            return "redirect:/booking/my-bookings";
        }

        // Check booking ownership
        String bookingUserId = booking.getUser() != null ? booking.getUser().getId() : null;
        if (bookingUserId == null || !bookingUserId.equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy booking!");
            return "redirect:/booking/my-bookings";
        }

        if (booking.getStatus() != Booking.BookingStatus.Pending) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy booking đang chờ duyệt!");
            return "redirect:/booking/my-bookings";
        }

        booking.setStatus(Booking.BookingStatus.Cancelled);
        booking.setCancelReason(cancelReason != null ? cancelReason : "Khách hàng hủy");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã hủy booking thành công!");
        return "redirect:/booking/my-bookings";
    }
}