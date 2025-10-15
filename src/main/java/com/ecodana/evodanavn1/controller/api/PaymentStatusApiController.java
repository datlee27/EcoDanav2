package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentStatusApiController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Check payment status for a booking
     */
    @GetMapping("/check-status")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@RequestParam String bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();
            
            // Check if booking has completed payment
            boolean hasCompletedPayment = paymentService.hasCompletedPayment(booking);
            
            // Get payment status
            String paymentStatus = booking.getPaymentStatusString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getBookingId());
            response.put("bookingCode", booking.getBookingCode());
            response.put("status", paymentStatus);
            response.put("hasCompletedPayment", hasCompletedPayment);
            response.put("totalAmount", booking.getTotalAmount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to check payment status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Manually mark payment as completed (for testing or manual confirmation)
     */
    @PostMapping("/mark-completed")
    public ResponseEntity<Map<String, Object>> markPaymentCompleted(@RequestParam String bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();
            
            // Find payment for this booking
            List<Payment> payments = paymentService.getPaymentsByBooking(booking);
            if (!payments.isEmpty()) {
                Payment payment = payments.get(0); // Get the first payment
                payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                payment.setPaymentDate(java.time.LocalDateTime.now());
                paymentService.savePayment(payment);
            }
            
            // Update booking
            booking.setStatus(Booking.BookingStatus.Approved);
            bookingService.updateBooking(booking);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment marked as completed");
            response.put("bookingId", booking.getBookingId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
