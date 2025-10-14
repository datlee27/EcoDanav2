package com.ecodana.evodanavn1.controller.staff;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.PaymentService;
import com.ecodana.evodanavn1.service.NotificationService;
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
@RequestMapping("/staff/payment-confirmation")
public class StaffPaymentConfirmationController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Hiển thị danh sách booking chờ xác nhận thanh toán
     */
    @GetMapping
    public String showPendingPaymentConfirmation(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User staff = (User) session.getAttribute("currentUser");
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        // Lấy danh sách booking đã thanh toán nhưng chưa xác nhận
        List<Booking> pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Pending && "Paid".equals(b.getPaymentStatus()))
                .collect(Collectors.toList());

        model.addAttribute("bookings", pendingBookings);
        model.addAttribute("currentUser", staff);

        return "staff/payment-confirmation";
    }

    /**
     * Xác nhận đã nhận tiền - Gửi request đến Owner
     */
    @PostMapping("/confirm/{bookingId}")
    public String confirmPayment(@PathVariable String bookingId,
                                 @RequestParam(required = false) String note,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User staff = (User) session.getAttribute("currentUser");
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/staff/payment-confirmation";
            }

            Booking booking = bookingOpt.get();

            // Kiểm tra trạng thái
            if (booking.getStatus() != Booking.BookingStatus.Pending || !"Paid".equals(booking.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("error", "Đơn đặt xe không ở trạng thái chờ xác nhận!");
                return "redirect:/staff/payment-confirmation";
            }

            // Cập nhật: Staff đã xác nhận nhận tiền, chuyển sang chờ Owner duyệt
            booking.setHandledBy(staff);
            booking.setStatus(Booking.BookingStatus.Pending); // Vẫn là Pending nhưng đã xác nhận payment
            bookingService.updateBooking(booking);

            // Gửi thông báo cho Owner để duyệt booking
            // TODO: Tìm Owner của vehicle này
            notificationService.createNotificationForAllAdmins(
                "Đơn đặt xe " + booking.getBookingCode() + " đã được Staff xác nhận thanh toán. Vui lòng duyệt đơn.",
                booking.getBookingId(),
                "BOOKING_APPROVAL"
            );

            redirectAttributes.addFlashAttribute("success", "Đã xác nhận thanh toán! Đơn đặt xe đã được gửi đến Owner để duyệt.");
            return "redirect:/staff/payment-confirmation";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/payment-confirmation";
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
        User staff = (User) session.getAttribute("currentUser");
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/staff/payment-confirmation";
        }

        Booking booking = bookingOpt.get();
        
        // Lấy thông tin payment
        Optional<Payment> paymentOpt = paymentService.findByBookingId(bookingId);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", paymentOpt.orElse(null));
        model.addAttribute("vehicle", booking.getVehicle());
        model.addAttribute("customer", booking.getUser());
        model.addAttribute("currentUser", staff);

        return "staff/payment-detail";
    }
}
