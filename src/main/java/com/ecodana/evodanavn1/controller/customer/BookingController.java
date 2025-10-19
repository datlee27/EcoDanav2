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
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.DiscountService;
import com.ecodana.evodanavn1.service.NotificationService;
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
            
            if (bookingRequest.getDiscountId() != null && !bookingRequest.getDiscountId().isEmpty()) {
                Discount discount = discountService.findByVoucherCode(bookingRequest.getDiscountId()).orElse(null);
                if (discount != null && discountService.isDiscountValid(discount)) {
                    discountCode = discount.getVoucherCode();
                    
                    // Calculate subtotal before discount
                    BigDecimal subtotal = rentalPrice.add(basicInsurance).add(additionalInsuranceAmount);
                    
                    // Calculate discount amount
                    discountAmount = discountService.calculateDiscountAmount(discount, subtotal);
                }
            }
            
            // Calculate total amount
            BigDecimal totalAmount = rentalPrice.add(basicInsurance).add(additionalInsuranceAmount).subtract(discountAmount);
            
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
            model.addAttribute("discountId", bookingRequest.getDiscountId());
            model.addAttribute("discountCode", discountCode);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("totalAmount", totalAmount);
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

            // Handle discount if provided
            Discount discount = null;
            if (bookingRequest.getDiscountId() != null && !bookingRequest.getDiscountId().isEmpty()) {
                discount = discountService.findByVoucherCode(bookingRequest.getDiscountId())
                    .orElse(null);
                
                // Validate discount again on server side
                if (discount != null && discountService.isDiscountValid(discount)) {
                    // Increment usage count
                    discount.setUsedCount(discount.getUsedCount() + 1);
                    discountService.updateDiscount(discount);
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
            booking.setStatus(Booking.BookingStatus.Pending);
            booking.setBookingCode("BK" + System.currentTimeMillis());
            booking.setRentalType(Booking.RentalType.daily);
            booking.setCreatedDate(LocalDateTime.now());
            booking.setTermsAgreed(true);
            booking.setTermsAgreedAt(LocalDateTime.now());
            booking.setExpectedPaymentMethod(bookingRequest.getPaymentMethod() != null ? bookingRequest.getPaymentMethod() : "Cash");
            booking.setDiscount(discount);

            bookingService.addBooking(booking);
            
            // Create notification for all admins
            try {
                String customerName = user.getUsername(); // Use username if fullName not available
                String notificationMessage = String.format(
                    "Đơn đặt xe mới #%s - Khách hàng: %s - Xe: %s - Tổng: %,d ₫",
                    booking.getBookingCode(),
                    customerName,
                    vehicle.getVehicleModel(),
                    bookingRequest.getTotalAmount().longValue()
                );
                notificationService.createNotificationForAllAdmins(
                    notificationMessage, 
                    booking.getBookingId(), 
                    "BOOKING"
                );
            } catch (Exception notifError) {
                System.out.println("Warning: Failed to create notification: " + notifError.getMessage());
                // Continue anyway - notification failure shouldn't block booking
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
        BigDecimal depositAmount = totalAmount.multiply(new BigDecimal("0.2")); // 20%
        BigDecimal remainingAmount = totalAmount.multiply(new BigDecimal("0.8")); // 80%
        
        booking.setDepositAmountRequired(depositAmount);
        booking.setRemainingAmount(remainingAmount);
        bookingService.updateBooking(booking);

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

        // Check cancellation policy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pickupDateTime = booking.getPickupDateTime();
        LocalDateTime createdDate = booking.getCreatedDate();
        
        // Calculate hours until pickup
        long hoursUntilPickup = java.time.Duration.between(now, pickupDateTime).toHours();
        
        // Calculate hours from booking creation to pickup
        long hoursFromCreationToPickup = java.time.Duration.between(createdDate, pickupDateTime).toHours();
        
        // Rule 1: If booked less than 24 hours before pickup, cannot cancel
        if (hoursFromCreationToPickup < 24) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy! Đơn đặt xe trong vòng 24 giờ trước ngày nhận không được hủy.");
            return "redirect:/booking/my-bookings";
        }
        
        // Rule 2: If booked 1-2 days before pickup, must cancel at least 8 hours before
        if (hoursFromCreationToPickup >= 24 && hoursFromCreationToPickup < 48) {
            if (hoursUntilPickup < 8) {
                redirectAttributes.addFlashAttribute("error", "Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 8 tiếng.");
                return "redirect:/booking/my-bookings";
            }
        }
        
        // Rule 3: If booked more than 2 days before pickup, must cancel at least 24 hours before
        if (hoursFromCreationToPickup >= 48) {
            if (hoursUntilPickup < 24) {
                redirectAttributes.addFlashAttribute("error", "Không thể hủy! Phải hủy trước ngày nhận xe ít nhất 24 tiếng.");
                return "redirect:/booking/my-bookings";
            }
        }

        String reason = cancelReason != null ? cancelReason : "Khách hàng hủy";
        bookingService.cancelBooking(bookingId, reason);

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

        if (booking.getStatus() != Booking.BookingStatus.Confirmed) {
            System.out.println("ERROR: Booking status is not Confirmed. Current status: " + booking.getStatus());
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy booking đã thanh toán! Trạng thái hiện tại: " + booking.getStatus());
            return "redirect:/booking/my-bookings";
        }

        // Check cancellation policy for confirmed bookings
        // Sử dụng createdDate làm thời điểm thanh toán (vì không có field riêng)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime paymentDate = booking.getCreatedDate();

        if (paymentDate == null) {
            redirectAttributes.addFlashAttribute("error", "Không thể xác định thời gian đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        // Calculate hours from payment (using createdDate as reference)
        long hoursFromPayment = java.time.Duration.between(paymentDate, now).toHours();

        // Check if within 2 hours of payment
        if (hoursFromPayment > 2) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy! Đã quá 2 tiếng kể từ lúc đặt xe (mất 10% phí).");
            return "redirect:/booking/my-bookings";
        }

        String reason = cancelReason != null ? cancelReason : "Khách hàng hủy sau thanh toán";
        bookingService.cancelCar(bookingId, reason);

        redirectAttributes.addFlashAttribute("success", "Đã hủy xe thành công! Sẽ hoàn tiền theo chính sách.");
        return "redirect:/booking/my-bookings";
    }
}