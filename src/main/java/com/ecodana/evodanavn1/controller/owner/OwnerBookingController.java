package com.ecodana.evodanavn1.controller.owner;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.PaymentService;
import com.ecodana.evodanavn1.service.NotificationService;
import com.ecodana.evodanavn1.service.WebSocketNotificationService;
import com.ecodana.evodanavn1.service.EmailNotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/bookings")
public class OwnerBookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;

    /**
     * Hiển thị danh sách booking chờ duyệt
     */
    @GetMapping("/pending-approval")
    public String showPendingApprovalBookings(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        // Lấy danh sách booking chờ duyệt (Pending và đã thanh toán)
        List<Booking> pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Pending && 
                            "Paid".equals(b.getPaymentStatus()))
                .collect(Collectors.toList());

        model.addAttribute("bookings", pendingBookings);
        model.addAttribute("currentUser", owner);

        return "owner/pending-bookings";
    }

    /**
     * Duyệt booking
     */
    @PostMapping("/approve/{bookingId}")
    public String approveBooking(@PathVariable String bookingId,
                                 @RequestParam(required = false) String note,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/owner/bookings/pending-approval";
            }

            Booking booking = bookingOpt.get();

            // Kiểm tra trạng thái
            if (booking.getStatus() != Booking.BookingStatus.Pending || !"Paid".equals(booking.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Đơn đặt xe không ở trạng thái chờ duyệt!");
                return "redirect:/owner/bookings/pending-approval";
            }

            // Cập nhật trạng thái
            booking.setStatus(Booking.BookingStatus.Approved);
            booking.setHandledBy(owner);
            bookingService.updateBooking(booking);

            // Gửi thông báo cho Customer
            notificationService.createNotification(
                booking.getUser().getId(),
                "Đơn đặt xe " + booking.getBookingCode() + " đã được chấp nhận! Vui lòng đến nhận xe đúng giờ."
            );

            // Gửi thông báo cho Staff (nếu cần)
            // TODO: Implement staff notification

            redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn đặt xe thành công!");
            return "redirect:/owner/bookings/pending-approval";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/owner/bookings/pending-approval";
        }
    }

    /**
     * Từ chối booking và hoàn tiền tự động
     */
    @PostMapping("/reject/{bookingId}")
    public String rejectBooking(@PathVariable String bookingId,
                               @RequestParam String rejectionReason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/owner/bookings/pending-approval";
            }

            Booking booking = bookingOpt.get();

            // Kiểm tra trạng thái
            if (booking.getStatus() != Booking.BookingStatus.Pending || !"Paid".equals(booking.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Đơn đặt xe không ở trạng thái chờ duyệt!");
                return "redirect:/owner/bookings/pending-approval";
            }

            // Cập nhật trạng thái booking
            booking.setStatus(Booking.BookingStatus.Rejected);
            booking.setCancelReason(rejectionReason);
            booking.setHandledBy(owner);
            bookingService.updateBooking(booking);

            // Tạo payment record cho refund
            Optional<Payment> originalPaymentOpt = paymentService.findByBookingId(bookingId);
            if (originalPaymentOpt.isPresent()) {
                Payment originalPayment = originalPaymentOpt.get();
                
                // Tạo payment refund
                Payment refundPayment = paymentService.createPayment(
                    booking,
                    booking.getUser(),
                    booking.getTotalAmount(),
                    originalPayment.getPaymentMethod(),
                    Payment.PaymentType.Refund,
                    "REFUND-" + booking.getBookingCode()
                );
                
                refundPayment.setPaymentStatus(Payment.PaymentStatus.Completed);
                refundPayment.setPaymentDate(java.time.LocalDateTime.now());
                refundPayment.setNotes("Hoàn tiền do Owner từ chối: " + rejectionReason);
                paymentService.savePayment(refundPayment);

                // Cập nhật trạng thái booking
                booking.setPaymentStatus("Refunded");
                booking.setStatus(Booking.BookingStatus.Cancelled);
                bookingService.updateBooking(booking);
            }

            // Gửi thông báo cho Customer
            notificationService.createNotification(
                booking.getUser().getId(),
                "Đơn đặt xe " + booking.getBookingCode() + " đã bị từ chối. Lý do: " + rejectionReason + ". Tiền đã được hoàn lại vào tài khoản của bạn."
            );

            // ========== GỬI THÔNG BÁO CHO STAFF ==========
            
            // 1. WebSocket Notification (Realtime)
            webSocketNotificationService.notifyStaffRefund(
                booking.getBookingCode(),
                booking.getUser().getFirstName() + " " + booking.getUser().getLastName(),
                booking.getTotalAmount().doubleValue(),
                rejectionReason
            );
            
            // 2. Email Notification
            emailNotificationService.sendRefundNotificationToStaff(
                "staff@ecodana.com", // TODO: Lấy từ config hoặc gửi cho tất cả staff
                booking.getBookingCode(),
                booking.getUser().getFirstName() + " " + booking.getUser().getLastName(),
                booking.getTotalAmount().doubleValue(),
                rejectionReason
            );
            
            // 3. Database Notification (Persistent)
            notificationService.createNotificationForAllStaff(
                "Cần xử lý hoàn tiền cho đơn " + booking.getBookingCode() + " - Khách hàng: " + 
                booking.getUser().getFirstName() + " " + booking.getUser().getLastName() + 
                " - Số tiền: " + String.format("%,.0f", booking.getTotalAmount()) + " ₫",
                booking.getBookingId(),
                "REFUND"
            );

            redirectAttributes.addFlashAttribute("success", "Đã từ chối đơn đặt xe và hoàn tiền thành công!");
            return "redirect:/owner/bookings/pending-approval";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/owner/bookings/pending-approval";
        }
    }

    /**
     * Xem chi tiết booking
     */
    @GetMapping("/detail/{bookingId}")
    public String viewBookingDetail(@PathVariable String bookingId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User owner = (User) session.getAttribute("currentUser");
        if (owner == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/owner/bookings/pending-approval";
        }

        Booking booking = bookingOpt.get();
        
        // Lấy thông tin payment
        Optional<Payment> paymentOpt = paymentService.findByBookingId(bookingId);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", paymentOpt.orElse(null));
        model.addAttribute("vehicle", booking.getVehicle());
        model.addAttribute("customer", booking.getUser());
        model.addAttribute("currentUser", owner);

        return "owner/booking-detail";
    }
}
