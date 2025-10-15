package com.ecodana.evodanavn1.controller.staff;

import com.ecodana.evodanavn1.model.*;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.PaymentService;
import com.ecodana.evodanavn1.service.StaffService;
import com.ecodana.evodanavn1.service.VehicleService;
import com.ecodana.evodanavn1.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Staff Dashboard
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Debug logging
        System.out.println("=== STAFF DASHBOARD DEBUG ===");
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("FirstName: " + currentUser.getFirstName());
        System.out.println("LastName: " + currentUser.getLastName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Role: " + (currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "null"));
        System.out.println("============================");

        // Get pending bookings
        List<Booking> pendingBookings = staffService.getPendingBookings();
        List<Booking> approvedBookings = staffService.getApprovedBookings();
        List<Booking> ongoingBookings = staffService.getOngoingBookings();

        // Get staff statistics
        Map<String, Object> stats = staffService.getStaffStatistics(currentUser.getId());

        // Add both currentUser and user for compatibility with different templates
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("approvedBookings", approvedBookings);
        model.addAttribute("ongoingBookings", ongoingBookings);
        model.addAttribute("stats", stats);

        return "staff/staff-dashboard";
    }

    /**
     * View pending bookings
     */
    @GetMapping("/bookings/pending")
    public String viewPendingBookings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Booking> pendingBookings = staffService.getPendingBookings();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookings", pendingBookings);
        model.addAttribute("pageTitle", "Đơn Đặt Xe Chờ Duyệt");
        model.addAttribute("bookingType", "pending");

        return "staff/booking-list";
    }

    /**
     * View approved bookings
     */
    @GetMapping("/bookings/approved")
    public String viewApprovedBookings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Booking> approvedBookings = staffService.getApprovedBookings();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookings", approvedBookings);
        model.addAttribute("pageTitle", "Đơn Đặt Xe Đã Duyệt");
        model.addAttribute("bookingType", "approved");

        return "staff/booking-list";
    }

    /**
     * View ongoing bookings
     */
    @GetMapping("/bookings/ongoing")
    public String viewOngoingBookings(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Booking> ongoingBookings = staffService.getOngoingBookings();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("bookings", ongoingBookings);
        model.addAttribute("pageTitle", "Đơn Đặt Xe Đang Diễn Ra");
        model.addAttribute("bookingType", "ongoing");

        return "staff/booking-list";
    }

    /**
     * View booking details
     */
    @GetMapping("/bookings/{bookingId}")
    public String viewBookingDetails(@PathVariable String bookingId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Map<String, Object> details = staffService.getBookingDetails(bookingId);
        if (details.isEmpty() || !details.containsKey("booking")) {
            return "redirect:/staff/dashboard";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("booking", details.get("booking"));
        model.addAttribute("approval", details.get("approval"));
        model.addAttribute("conditionLogs", details.get("conditionLogs"));

        return "staff/booking-details";
    }

    /**
     * Approve booking
     */
    @PostMapping("/bookings/{bookingId}/approve")
    public String approveBooking(
            @PathVariable String bookingId,
            @RequestParam(required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            staffService.approveBooking(bookingId, currentUser, note);
            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn đặt xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/staff/bookings/pending";
    }

    /**
     * Reject booking
     */
    @PostMapping("/bookings/{bookingId}/reject")
    public String rejectBooking(
            @PathVariable String bookingId,
            @RequestParam String rejectionReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập lý do từ chối");
            }
            staffService.rejectBooking(bookingId, currentUser, rejectionReason);
            redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn đặt xe!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/staff/bookings/pending";
    }

    /**
     * Show vehicle pickup form
     */
    @GetMapping("/bookings/{bookingId}/pickup")
    public String showPickupForm(@PathVariable String bookingId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/staff/dashboard";
        }

        Booking booking = bookingOpt.get();
        
        // Check if pickup already recorded
        Optional<VehicleConditionLogs> pickupLog = staffService.getPickupLog(bookingId);
        if (pickupLog.isPresent()) {
            model.addAttribute("error", "Xe đã được giao cho khách hàng!");
            return "redirect:/staff/bookings/" + bookingId;
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", booking.getVehicle());
        model.addAttribute("actionType", "pickup");

        return "staff/vehicle-handover";
    }

    /**
     * Process vehicle pickup
     */
    @PostMapping("/bookings/{bookingId}/pickup")
    public String processPickup(
            @PathVariable String bookingId,
            @RequestParam Integer odometer,
            @RequestParam String batteryLevel,
            @RequestParam String conditionStatus,
            @RequestParam(required = false) String conditionDescription,
            @RequestParam(required = false) String damageImages,
            @RequestParam(required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            staffService.recordPickupCondition(
                bookingId, 
                currentUser, 
                odometer, 
                batteryLevel, 
                conditionStatus, 
                conditionDescription, 
                damageImages, 
                note
            );
            redirectAttributes.addFlashAttribute("success", "Đã ghi nhận giao xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/bookings/" + bookingId + "/pickup";
        }

        return "redirect:/staff/bookings/ongoing";
    }

    /**
     * Show vehicle return form
     */
    @GetMapping("/bookings/{bookingId}/return")
    public String showReturnForm(@PathVariable String bookingId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/staff/dashboard";
        }

        Booking booking = bookingOpt.get();
        
        // Check if return already recorded
        Optional<VehicleConditionLogs> returnLog = staffService.getReturnLog(bookingId);
        if (returnLog.isPresent()) {
            model.addAttribute("error", "Xe đã được trả!");
            return "redirect:/staff/bookings/" + bookingId;
        }

        // Get pickup log for comparison
        Optional<VehicleConditionLogs> pickupLog = staffService.getPickupLog(bookingId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", booking.getVehicle());
        model.addAttribute("pickupLog", pickupLog.orElse(null));
        model.addAttribute("actionType", "return");

        return "staff/vehicle-handover";
    }

    /**
     * Process vehicle return
     */
    @PostMapping("/bookings/{bookingId}/return")
    public String processReturn(
            @PathVariable String bookingId,
            @RequestParam Integer odometer,
            @RequestParam String batteryLevel,
            @RequestParam String conditionStatus,
            @RequestParam(required = false) String conditionDescription,
            @RequestParam(required = false) String damageImages,
            @RequestParam(required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            staffService.recordReturnCondition(
                bookingId, 
                currentUser, 
                odometer, 
                batteryLevel, 
                conditionStatus, 
                conditionDescription, 
                damageImages, 
                note
            );
            redirectAttributes.addFlashAttribute("success", "Đã ghi nhận trả xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/bookings/" + bookingId + "/return";
        }

        return "redirect:/staff/dashboard";
    }

    /**
     * View all vehicles
     */
    @GetMapping("/vehicles")
    public String viewVehicles(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("vehicles", vehicles);

        return "staff/vehicle-list";
    }

    /**
     * View vehicle details
     */
    @GetMapping("/vehicles/{vehicleId}")
    public String viewVehicleDetails(@PathVariable String vehicleId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            return "redirect:/staff/vehicles";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        model.addAttribute("vehicle", vehicleOpt.get());

        return "staff/vehicle-details";
    }

    /**
     * Maintenance page
     */
    @GetMapping("/maintenance")
    public String maintenance(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);

        return "staff/maintenance";
    }

    /**
     * Reports page
     */
    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);

        return "staff/reports";
    }

    /**
     * Customers page
     */
    @GetMapping("/customers")
    public String customers(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);

        return "staff/customers";
    }

    // ============================================
    // Payment Flow V2 Endpoints
    // ============================================

    /**
     * Confirm payment received from customer (staff holds money temporarily)
     */
    @PostMapping("/booking/{bookingId}/confirm-payment-received")
    public String confirmPaymentReceived(@PathVariable String bookingId,
                                        @RequestParam(required = false) String notes,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Find booking
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/staff/bookings/pending";
            }

            Booking booking = bookingOpt.get();

            // Find payment record
            Optional<Payment> paymentOpt = paymentService.findByBookingId(bookingId);
            if (!paymentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin thanh toán!");
                return "redirect:/staff/booking/" + bookingId;
            }

            Payment payment = paymentOpt.get();

            // Update payment status - Payment completed
            payment.setPaymentStatus(Payment.PaymentStatus.Completed);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setUser(currentUser);
            payment.setNotes(notes != null ? notes : "Đã nhận chuyển khoản từ khách hàng");
            paymentService.savePayment(payment);

            // Update booking status - Payment received, waiting for owner approval
            booking.setHandledBy(currentUser);
            bookingService.updateBooking(booking);

            // Send notification to owner
            try {
                String ownerMessage = String.format(
                    "🔔 Đơn đặt xe #%s đã được thanh toán! Vui lòng duyệt đơn. Xe: %s, Khách: %s",
                    booking.getBookingCode(),
                    booking.getVehicle().getVehicleModel(),
                    booking.getUser().getUsername()
                );
                // Get vehicle owner and send notification
                User vehicleOwner = booking.getVehicle().getLastUpdatedBy();
                if (vehicleOwner != null) {
                    notificationService.createNotificationForOwner(
                        vehicleOwner.getId(),
                        ownerMessage,
                        bookingId,
                        "BOOKING_PAYMENT_RECEIVED"
                    );
                }
            } catch (Exception notifError) {
                System.out.println("Warning: Failed to send notification: " + notifError.getMessage());
            }

            redirectAttributes.addFlashAttribute("success", "Đã xác nhận nhận tiền. Chờ chủ xe duyệt đơn.");
            return "redirect:/staff/booking/" + bookingId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/booking/" + bookingId;
        }
    }

    /**
     * Refund payment to customer (when owner rejects)
     */
    @PostMapping("/booking/{bookingId}/refund-payment")
    public String refundPayment(@PathVariable String bookingId,
                               @RequestParam(required = false) String notes,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Find booking
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/staff/dashboard";
            }

            Booking booking = bookingOpt.get();

            // Check if booking is rejected
            if (booking.getStatus() != Booking.BookingStatus.Rejected) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể hoàn tiền cho đơn bị từ chối!");
                return "redirect:/staff/booking/" + bookingId;
            }

            // Find payment record
            Optional<Payment> paymentOpt = paymentService.findByBookingId(bookingId);
            if (!paymentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin thanh toán!");
                return "redirect:/staff/booking/" + bookingId;
            }

            Payment originalPayment = paymentOpt.get();

            // Create refund payment record
            Payment refund = new Payment();
            refund.setPaymentId(java.util.UUID.randomUUID().toString());
            refund.setBooking(booking);
            refund.setAmount(originalPayment.getAmount());
            refund.setPaymentMethod("BankTransfer");
            refund.setPaymentStatus(Payment.PaymentStatus.Completed);
            refund.setPaymentType(Payment.PaymentType.Refund);
            refund.setPaymentDate(LocalDateTime.now());
            refund.setUser(currentUser);
            refund.setNotes(notes != null ? notes : "Hoàn tiền do chủ xe từ chối");
            refund.setCreatedDate(LocalDateTime.now());
            paymentService.savePayment(refund);

            // Update booking
            bookingService.updateBooking(booking);

            redirectAttributes.addFlashAttribute("success", "Đã hoàn tiền cho khách hàng thành công!");
            return "redirect:/staff/booking/" + bookingId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/booking/" + bookingId;
        }
    }

    /**
     * Transfer money to owner (after trip completed)
     */
    @PostMapping("/booking/{bookingId}/transfer-to-owner")
    public String transferToOwner(@PathVariable String bookingId,
                                 @RequestParam(required = false) String notes,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Find booking
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/staff/dashboard";
            }

            Booking booking = bookingOpt.get();

            // Check if booking is completed
            if (booking.getStatus() != Booking.BookingStatus.Completed) {
                redirectAttributes.addFlashAttribute("error", "Chỉ chuyển tiền khi đơn đã hoàn thành!");
                return "redirect:/staff/booking/" + bookingId;
            }

            // Find original payment record
            Optional<Payment> paymentOpt = paymentService.findByBookingId(bookingId);
            if (!paymentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin thanh toán!");
                return "redirect:/staff/booking/" + bookingId;
            }

            Payment originalPayment = paymentOpt.get();

            // Create transfer payment record
            Payment transfer = new Payment();
            transfer.setPaymentId(java.util.UUID.randomUUID().toString());
            transfer.setBooking(booking);
            transfer.setAmount(originalPayment.getAmount());
            transfer.setPaymentMethod("BankTransfer");
            transfer.setPaymentStatus(Payment.PaymentStatus.Completed);
            transfer.setPaymentType(Payment.PaymentType.FinalPayment);
            transfer.setPaymentDate(LocalDateTime.now());
            transfer.setUser(currentUser);
            transfer.setNotes(notes != null ? notes : "Chuyển tiền cho chủ xe");
            transfer.setCreatedDate(LocalDateTime.now());
            paymentService.savePayment(transfer);

            // Update booking
            bookingService.updateBooking(booking);

            redirectAttributes.addFlashAttribute("success", "Đã chuyển tiền cho chủ xe thành công!");
            return "redirect:/staff/booking/" + bookingId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/booking/" + bookingId;
        }
    }

    /**
     * Confirm vehicle pickup
     */
    @PostMapping("/booking/{bookingId}/confirm-pickup")
    public String confirmPickup(@PathVariable String bookingId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Find booking
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/staff/dashboard";
            }

            Booking booking = bookingOpt.get();

            // Check if booking is approved
            if (booking.getStatus() != Booking.BookingStatus.Approved) {
                redirectAttributes.addFlashAttribute("error", "Đơn đặt xe chưa được duyệt!");
                return "redirect:/staff/booking/" + bookingId;
            }

            // Update booking status - Ongoing
            booking.setStatus(Booking.BookingStatus.Ongoing);
            bookingService.updateBooking(booking);

            redirectAttributes.addFlashAttribute("success", "Đã xác nhận giao xe. Chúc khách hàng có chuyến đi vui vẻ!");
            return "redirect:/staff/booking/" + bookingId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/booking/" + bookingId;
        }
    }

    /**
     * Legacy endpoint - Keep for backward compatibility
     * @deprecated Use confirmPaymentReceived instead
     */
    @Deprecated
    @PostMapping("/booking/{bookingId}/confirm-payment")
    public String confirmPayment(@PathVariable String bookingId,
                                @RequestParam(required = false) String notes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // Redirect to new endpoint
        return confirmPaymentReceived(bookingId, notes, session, redirectAttributes);
    }
}
