package com.ecodana.evodanavn1.controller.customer;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.UserFeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private UserFeedbackService userFeedbackService;

    @Autowired
    private BookingService bookingService;

    @PostMapping("/submit/{bookingId}")
    public String submitFeedback(@PathVariable String bookingId,
                                @RequestParam int rating,
                                @RequestParam(required = false) String content,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đánh giá");
            return "redirect:/login";
        }

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe");
            return "redirect:/booking/my-bookings";
        }

        // Check if booking belongs to current user and is completed
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền đánh giá đơn đặt xe này");
            return "redirect:/booking/my-bookings";
        }

        if (booking.getStatus() != Booking.BookingStatus.Completed) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể đánh giá các đơn đặt xe đã hoàn thành");
            return "redirect:/booking/my-bookings";
        }

        // Check if feedback already exists
        if (userFeedbackService.hasFeedbackForBooking(booking)) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá đơn đặt xe này rồi");
            return "redirect:/booking/my-bookings";
        }

        try {
            userFeedbackService.createFeedback(currentUser, booking, rating, content);
            redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá! Đánh giá của bạn đã được gửi thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.");
        }

        return "redirect:/booking/my-bookings";
    }
}
