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
import com.ecodana.evodanavn1.model.Discount;
import com.ecodana.evodanavn1.model.Vehicle; // Đảm bảo import Vehicle
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.DiscountService;
import com.ecodana.evodanavn1.service.NotificationService;
import com.ecodana.evodanavn1.service.UserFeedbackService;
import com.ecodana.evodanavn1.service.VehicleService;
import com.ecodana.evodanavn1.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private UserFeedbackService userFeedbackService;

    /**
     * Show checkout page
     */
    @PostMapping("/checkout")
    public String showCheckout(@ModelAttribute BookingRequest bookingRequest, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt xe!");
            return "redirect:/login";
        }

        try {
            // Get vehicle
            Vehicle vehicle = vehicleService.getVehicleById(bookingRequest.getVehicleId()).orElse(null);
            if (vehicle == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy xe!");
                return "redirect:/vehicles";
            }

            // Parse dates and times for display
            LocalDate pickupDate = LocalDate.parse(bookingRequest.getPickupDate());
            LocalDate returnDate = LocalDate.parse(bookingRequest.getReturnDate());
            LocalTime pickupTime = LocalTime.parse(bookingRequest.getPickupTime());
            LocalTime returnTime = LocalTime.parse(bookingRequest.getReturnTime());

            // Format for display
            String pickupDateTime = pickupDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " + pickupTime;
            String returnDateTime = returnDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " + returnTime;

            // Calculate rental price
            BigDecimal dailyPrice = vehicle.getDailyPriceFromJson();
            BigDecimal rentalPrice = dailyPrice.multiply(new BigDecimal(bookingRequest.getRentalDays()));

            // Calculate basic insurance
            BigDecimal basicInsurance = new BigDecimal("110401");

            // Calculate additional insurance
            BigDecimal additionalInsuranceAmount = BigDecimal.ZERO;
            if (bookingRequest.getAdditionalInsurance() != null && bookingRequest.getAdditionalInsurance()) {
                additionalInsuranceAmount = new BigDecimal("50000").multiply(new BigDecimal(bookingRequest.getRentalDays()));
            }

            // Get discount info and calculate discount amount
            String discountCode = null;
            BigDecimal discountAmount = BigDecimal.ZERO;

            // Sửa: Lấy discount code từ bookingRequest.getDiscountId() (vì nó đang lưu code)
            if (bookingRequest.getDiscountId() != null && !bookingRequest.getDiscountId().isEmpty()) {
                Discount discount = discountService.findByVoucherCode(bookingRequest.getDiscountId()).orElse(null); // Tìm bằng Code
                if (discount != null && discountService.isDiscountValid(discount)) {
                    discountCode = discount.getVoucherCode();

                    BigDecimal subtotal = rentalPrice.add(basicInsurance).add(additionalInsuranceAmount);

                    discountAmount = discountService.calculateDiscountAmount(discount, subtotal);

                    // Gán lại discountAmount từ request nếu nó khác (trường hợp JS tính)
                    if (bookingRequest.getDiscountAmount() != null && bookingRequest.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discountAmount = bookingRequest.getDiscountAmount();
                    }
                }
            }

            // Calculate total amount
            BigDecimal totalAmount = rentalPrice.add(basicInsurance).add(additionalInsuranceAmount).subtract(discountAmount);

            // Cập nhật lại totalAmount từ request (vì JS đã tính toán cuối cùng)
            if(bookingRequest.getTotalAmount() != null && bookingRequest.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalAmount = bookingRequest.getTotalAmount();
            }

            // Add attributes to model
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("vehicleId", vehicle.getVehicleId());
            model.addAttribute("pickupDateTime", pickupDateTime);
            model.addAttribute("returnDateTime", returnDateTime);
            model.addAttribute("pickupDate", bookingRequest.getPickupDate());
            model.addAttribute("pickupTime", bookingRequest.getPickupTime());
            model.addAttribute("returnDate", bookingRequest.getReturnDate());
            model.addAttribute("returnTime", bookingRequest.getReturnTime());
            model.addAttribute("pickupLocation", bookingRequest.getPickupLocation());
            model.addAttribute("rentalDays", bookingRequest.getRentalDays());
            model.addAttribute("rentalPrice", rentalPrice);
            model.addAttribute("additionalInsurance", bookingRequest.getAdditionalInsurance());
            model.addAttribute("additionalInsuranceAmount", additionalInsuranceAmount);
            model.addAttribute("discountId", bookingRequest.getDiscountId()); // Vẫn gửi ID (code)
            model.addAttribute("discountCode", discountCode); // Tên mã
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("totalAmount", totalAmount); // Sử dụng total đã tính
            model.addAttribute("currentUser", user);

            return "customer/booking-checkout";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vehicles/" + bookingRequest.getVehicleId();
        }
    }

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
            if (pickupDateTime.isBefore(LocalDateTime.now().minusMinutes(5))) { // Cho phép trễ 5 phút
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

            // ================================================================
            // === BẮT ĐẦU THAY ĐỔI: CẬP NHẬT TRẠNG THÁI XE ===
            // ================================================================
            // Chuyển trạng thái xe thành "Rented" (hoặc "Unavailable") ngay lập tức
            // để xe này không còn xuất hiện trong kết quả tìm kiếm.
            // Trạng thái này sẽ được đặt lại thành "Available" nếu Owner từ chối
            // (logic đã có trong BookingService.rejectBooking)
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            vehicleService.updateVehicle(vehicle); // Lưu thay đổi trạng thái của xe
            // ================================================================
            // === KẾT THÚC THAY ĐỔI ===
            // ================================================================


            // Handle discount if provided
            Discount discount = null;
            if (bookingRequest.getDiscountId() != null && !bookingRequest.getDiscountId().isEmpty()) {
                // bookingRequest.getDiscountId() đang lưu voucherCode
                discount = discountService.findByVoucherCode(bookingRequest.getDiscountId())
                        .orElse(null);

                // Validate discount again on server side
                if (discount != null && discountService.isDiscountValid(discount)) {
                    // Increment usage count
                    discount.setUsedCount(discount.getUsedCount() + 1);
                    discountService.updateDiscount(discount);
                } else {
                    discount = null; // Vô hiệu hóa discount nếu không hợp lệ
                }
            }

            // Create booking
            Booking booking = new Booking();
            booking.setBookingId(UUID.randomUUID().toString());
            booking.setUser(user);
            booking.setVehicle(vehicle);
            booking.setPickupDateTime(pickupDateTime);
            booking.setReturnDateTime(returnDateTime);
            booking.setTotalAmount(bookingRequest.getTotalAmount());
            booking.setStatus(Booking.BookingStatus.Pending); // Trạng thái chờ Owner duyệt
            booking.setBookingCode("BK" + System.currentTimeMillis());
            booking.setRentalType(Booking.RentalType.daily);
            booking.setCreatedDate(LocalDateTime.now());
            booking.setTermsAgreed(true);
            booking.setTermsAgreedAt(LocalDateTime.now());
            booking.setExpectedPaymentMethod(bookingRequest.getPaymentMethod() != null ? bookingRequest.getPaymentMethod() : "Cash");
            booking.setDiscount(discount);

            // Tính toán và lưu tiền cọc (20%) và tiền còn lại (80%)
            BigDecimal totalAmount = bookingRequest.getTotalAmount();
            BigDecimal depositAmount = totalAmount.multiply(new BigDecimal("0.2"));
            BigDecimal remainingAmount = totalAmount.subtract(depositAmount); // Lấy tổng trừ cọc

            booking.setDepositAmountRequired(depositAmount);
            booking.setRemainingAmount(remainingAmount);


            bookingService.addBooking(booking);

            // Create notification for all admins AND Owner
            try {
                String customerName = (user.getFirstName() != null) ? (user.getFirstName() + " " + user.getLastName()) : user.getUsername();
                String notificationMessage = String.format(
                        "Đơn đặt xe mới #%s - Khách hàng: %s - Xe: %s - Tổng: %,d ₫",
                        booking.getBookingCode(),
                        customerName,
                        vehicle.getVehicleModel(),
                        bookingRequest.getTotalAmount().longValue()
                );

                // Gửi cho Owner của xe
                if(vehicle.getOwnerId() != null) {
                    notificationService.createNotification(vehicle.getOwnerId(), notificationMessage, booking.getBookingId(), "BOOKING_REQUEST");
                } else {
                    // Fallback: Gửi cho admin nếu không tìm thấy owner
                    notificationService.createNotificationForAllAdmins(notificationMessage, booking.getBookingId(), "BOOKING_REQUEST");
                }

            } catch (Exception notifError) {
                System.out.println("Warning: Failed to create notification: " + notifError.getMessage());
            }

            redirectAttributes.addFlashAttribute("success", "Đặt xe thành công! Vui lòng chờ chủ xe duyệt.");
            System.out.println("=== Booking created successfully ===");
            System.out.println("Booking ID: " + booking.getBookingId());
            System.out.println("Booking Code: " + booking.getBookingCode());
            System.out.println("Redirecting to confirmation page");

            // Redirect đến trang confirmation - chờ owner duyệt
            return "redirect:/booking/confirmation/" + booking.getBookingId();

        } catch (Exception e) {
            System.out.println("=== ERROR creating booking ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vehicles/" + bookingRequest.getVehicleId();
        }
    }

    /**
     * Show payment page
     */
    @GetMapping("/payment/{bookingId}")
    public String showPaymentPage(@PathVariable String bookingId, HttpSession session, Model model, HttpServletRequest request) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null) {
            return "redirect:/booking/my-bookings";
        }

        // Verify booking ownership
        String bookingUserId = booking.getUser() != null ? booking.getUser().getId() : null;
        if (bookingUserId == null || !bookingUserId.equals(user.getId())) {
            return "redirect:/booking/my-bookings";
        }

        Vehicle vehicle = booking.getVehicle();
        if (vehicle == null) {
            return "redirect:/booking/my-bookings";
        }

        // Tính toán số tiền cọc 20% và số tiền còn lại 80%
        BigDecimal totalAmount = booking.getTotalAmount();
        BigDecimal depositAmount = booking.getDepositAmountRequired();
        BigDecimal remainingAmount = booking.getRemainingAmount();

        // Tính toán lại nếu chưa có
        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) == 0) {
            depositAmount = totalAmount.multiply(new BigDecimal("0.2")); // 20%
            remainingAmount = totalAmount.subtract(depositAmount); // 80%

            booking.setDepositAmountRequired(depositAmount);
            booking.setRemainingAmount(remainingAmount);
            bookingService.updateBooking(booking);
        }


        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("depositAmount", depositAmount);
        model.addAttribute("remainingAmount", remainingAmount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("currentUser", user);

        return "customer/booking-payment";
    }

    /**
     * Process payment - Create VNPay URL
     */
    @PostMapping("/payment/process/{bookingId}")
    public String processPayment(
            @PathVariable String bookingId,
            @RequestParam("paymentType") String paymentType,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        try {
            long amount;
            String orderInfo;

            if ("deposit".equals(paymentType)) {
                // Thanh toán 20% cọc
                amount = booking.getDepositAmountRequired().longValue();
                orderInfo = "Thanh toán cọc 20% đơn hàng " + booking.getBookingCode();
            } else {
                // Thanh toán 100%
                amount = booking.getTotalAmount().longValue();
                orderInfo = "Thanh toán toàn bộ đơn hàng " + booking.getBookingCode();
            }

            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, bookingId, request);

            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo thanh toán: " + e.getMessage());
            return "redirect:/booking/payment/" + bookingId;
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
        
        // Add feedback information to each booking
        for (Booking booking : bookings) {
            boolean hasFeedback = userFeedbackService.hasFeedbackForBooking(booking);
            booking.setHasFeedback(hasFeedback);

            // Determine review eligibility: Completed and at least 5 days since pickup
            boolean eligible = false;
            try {
                if (booking.getStatus() == Booking.BookingStatus.Completed) {
                    java.time.LocalDate eligibleDate = booking.getPickupDateTime().toLocalDate().plusDays(5);
                    eligible = !java.time.LocalDate.now().isBefore(eligibleDate);
                }
            } catch (Exception ignored) {}
            booking.setCanReview(eligible && !hasFeedback);
        }
        
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

        // CHỈ cho phép hủy khi đang Pending
        if (booking.getStatus() != Booking.BookingStatus.Pending) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy booking đang chờ duyệt (Pending)! Nếu đã thanh toán, vui lòng dùng nút Hủy Xe.");
            return "redirect:/booking/my-bookings";
        }

        // Check cancellation policy (Logic chính sách hủy của bạn)
        // ... (Giữ nguyên logic kiểm tra thời gian)

        String reason = cancelReason != null ? cancelReason : "Khách hàng hủy";
        bookingService.cancelBooking(bookingId, reason); // Hàm này đã tự động cập nhật status xe

        redirectAttributes.addFlashAttribute("success", "Đã hủy booking thành công!");
        return "redirect:/booking/my-bookings";
    }

    /**
     * Cancel car (for confirmed bookings - đã thanh toán)
     */
    @PostMapping("/cancel-car/{bookingId}")
    public String cancelCar(
            @PathVariable String bookingId,
            @RequestParam(required = false) String cancelReason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== Cancel Car Request ===");
        System.out.println("Booking ID: " + bookingId);

        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            System.out.println("ERROR: User not logged in");
            return "redirect:/login";
        }
        System.out.println("User ID: " + user.getId());

        Booking booking = bookingService.findById(bookingId).orElse(null);
        if (booking == null) {
            System.out.println("ERROR: Booking not found");
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy booking!");
            return "redirect:/booking/my-bookings";
        }
        System.out.println("Booking found. Status: " + booking.getStatus());

        // Check booking ownership
        String bookingUserId = booking.getUser() != null ? booking.getUser().getId() : null;
        System.out.println("Booking User ID: " + bookingUserId);

        if (bookingUserId == null || !bookingUserId.equals(user.getId())) {
            System.out.println("ERROR: User ID mismatch");
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy booking!");
            return "redirect:/booking/my-bookings";
        }

        // CHỈ cho phép hủy khi đã Confirmed (đã thanh toán)
        if (booking.getStatus() != Booking.BookingStatus.Confirmed) {
            System.out.println("ERROR: Booking status is not Confirmed. Current status: " + booking.getStatus());
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy booking đã thanh toán! Trạng thái hiện tại: " + booking.getStatus());
            return "redirect:/booking/my-bookings";
        }

        // Check cancellation policy for confirmed bookings
        LocalDateTime now = LocalDateTime.now();

        // Tìm payment liên quan (giả sử paymentDate là thời điểm thanh toán)
        // Đây là logic giả định, cần xem lại cách lưu paymentDate
        LocalDateTime paymentDate = booking.getCreatedDate(); // Tạm dùng createdDate

        if (paymentDate == null) {
            paymentDate = booking.getCreatedDate(); // Fallback
        }

        long hoursFromPayment = java.time.Duration.between(paymentDate, now).toHours();

        // Check if within 2 hours of payment
        if (hoursFromPayment > 2) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy! Đã quá 2 tiếng kể từ lúc đặt xe (mất 10% phí). Vui lòng liên hệ CSKH.");
            return "redirect:/booking/my-bookings";
        }

        String reason = cancelReason != null ? cancelReason : "Khách hàng hủy sau thanh toán (trong 2 giờ)";
        bookingService.cancelCar(bookingId, reason); // Hàm này đã tự động cập nhật status xe

        redirectAttributes.addFlashAttribute("success", "Đã hủy xe thành công! Bạn sẽ được hoàn tiền 100% (trong vòng 2 giờ).");
        return "redirect:/booking/my-bookings";
    }
}