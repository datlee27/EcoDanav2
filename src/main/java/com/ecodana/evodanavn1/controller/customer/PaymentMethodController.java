package com.ecodana.evodanavn1.controller.customer;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.service.BookingService;
import com.ecodana.evodanavn1.service.PaymentService;
import com.ecodana.evodanavn1.service.VNPayService;
import com.ecodana.evodanavn1.service.VehicleService;
import com.ecodana.evodanavn1.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentMethodController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Show payment method selection page
     */
    @GetMapping("/select-method/{bookingId}")
    public String selectPaymentMethod(@PathVariable String bookingId,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không có quyền truy cập!");
            return "redirect:/booking/my-bookings";
        }

        // Get vehicle
        Vehicle vehicle = booking.getVehicle();
        if (vehicle == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin xe!");
            return "redirect:/booking/my-bookings";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("currentUser", user);

        return "customer/payment-method-selection";
    }

    /**
     * Process payment method selection
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam String bookingId,
                                @RequestParam String paymentMethod,
                                HttpServletRequest request,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không có quyền truy cập!");
            return "redirect:/booking/my-bookings";
        }

        // Route to appropriate payment method
        switch (paymentMethod) {
            case "CASH":
                return processCashPayment(booking, user, redirectAttributes);
            
            case "BANK_TRANSFER":
                return processBankTransfer(booking, user, redirectAttributes);
            
            case "QR_CODE":
                return processQRPayment(booking, user, redirectAttributes);
            
            case "VNPAY":
                return processVNPayPayment(booking, user, request, redirectAttributes);
            
            case "CREDIT_CARD":
                return processCreditCardPayment(booking, user, redirectAttributes);
            
            default:
                redirectAttributes.addFlashAttribute("error", "Phương thức thanh toán không hợp lệ!");
                return "redirect:/payment/select-method/" + bookingId;
        }
    }

    /**
     * Process cash payment
     */
    private String processCashPayment(Booking booking, User user, RedirectAttributes redirectAttributes) {
        // Create payment record
        paymentService.createPayment(
            booking,
            user,
            booking.getTotalAmount(),
            "Cash",
            Payment.PaymentType.Deposit,
            booking.getBookingCode()
        );

        // Update booking
        booking.setExpectedPaymentMethod("Cash");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Đã chọn thanh toán bằng tiền mặt. Vui lòng thanh toán khi nhận xe.");
        return "redirect:/booking/confirmation/" + booking.getBookingId();
    }

    /**
     * Process bank transfer
     */
    private String processBankTransfer(Booking booking, User user, RedirectAttributes redirectAttributes) {
        // Create payment record
        paymentService.createPayment(
            booking,
            user,
            booking.getTotalAmount(),
            "Bank Transfer",
            Payment.PaymentType.Deposit,
            booking.getBookingCode()
        );

        // Update booking
        booking.setExpectedPaymentMethod("Bank Transfer");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("success", "Vui lòng chuyển khoản theo thông tin được cung cấp.");
        return "redirect:/payment/bank-transfer-info/" + booking.getBookingId();
    }

    /**
     * Process QR code payment
     */
    private String processQRPayment(Booking booking, User user, RedirectAttributes redirectAttributes) {
        // Create payment record
        paymentService.createPayment(
            booking,
            user,
            booking.getTotalAmount(),
            "QR Code",
            Payment.PaymentType.Deposit,
            booking.getBookingCode()
        );

        // Update booking
        booking.setExpectedPaymentMethod("QR Code");
        bookingService.updateBooking(booking);

        return "redirect:/payment/qr-code/" + booking.getBookingId();
    }

    /**
     * Process VNPay payment
     */
    private String processVNPayPayment(Booking booking, User user, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Create payment record
            paymentService.createPayment(
                booking,
                user,
                booking.getTotalAmount(),
                "VNPay",
                Payment.PaymentType.Deposit,
                booking.getBookingCode()
            );

            // Update booking
            booking.setExpectedPaymentMethod("VNPay");
            bookingService.updateBooking(booking);

            // Generate VNPay payment URL
            String paymentUrl = vnPayService.createPaymentUrl(booking, request);
            
            if (paymentUrl == null) {
                redirectAttributes.addFlashAttribute("error", "Không thể tạo link thanh toán!");
                return "redirect:/payment/select-method/" + booking.getBookingId();
            }

            // Redirect to VNPay
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/payment/select-method/" + booking.getBookingId();
        }
    }

    /**
     * Process credit card payment
     */
    private String processCreditCardPayment(Booking booking, User user, RedirectAttributes redirectAttributes) {
        // Create payment record
        paymentService.createPayment(
            booking,
            user,
            booking.getTotalAmount(),
            "Credit Card",
            Payment.PaymentType.Deposit,
            booking.getBookingCode()
        );

        // Update booking
        booking.setExpectedPaymentMethod("Credit Card");
        bookingService.updateBooking(booking);

        redirectAttributes.addFlashAttribute("info", "Chức năng thanh toán thẻ tín dụng đang được phát triển.");
        return "redirect:/booking/confirmation/" + booking.getBookingId();
    }

    /**
     * Show QR code payment page
     */
    @GetMapping("/qr-code/{bookingId}")
    public String showQRPayment(@PathVariable String bookingId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không có quyền truy cập!");
            return "redirect:/booking/my-bookings";
        }

        // Generate QR data (bank transfer info)
        String qrData = generateQRData(booking);

        model.addAttribute("booking", booking);
        model.addAttribute("qrData", qrData);
        model.addAttribute("currentUser", user);

        return "customer/payment-qr";
    }

    /**
     * Generate QR data for bank transfer
     */
    private String generateQRData(Booking booking) {
        // Format: Bank|Account|Amount|Content
        // Example for VietQR standard
        String bankCode = "970436"; // Vietcombank
        String accountNumber = "1234567890";
        String amount = booking.getTotalAmount().longValue() + "";
        String content = booking.getBookingCode();
        
        // VietQR format
        String qrData = String.format(
            "2|6|%s|%s|%s|%s|0|0|%s",
            bankCode,
            accountNumber,
            "CONG TY ECODANA",
            amount,
            content
        );
        
        try {
            return URLEncoder.encode(qrData, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return qrData;
        }
    }

    /**
     * Show bank transfer information page
     */
    @GetMapping("/bank-transfer-info/{bookingId}")
    public String showBankTransferInfo(@PathVariable String bookingId,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        Booking booking = bookingOpt.get();

        // Verify booking belongs to user
        if (!booking.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không có quyền truy cập!");
            return "redirect:/booking/my-bookings";
        }

        // Get vehicle
        Vehicle vehicle = booking.getVehicle();

        model.addAttribute("booking", booking);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("currentUser", user);

        return "customer/payment-bank-transfer";
    }

    /**
     * VNPay return callback
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Get all parameters from VNPay
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Booking Code
            String vnp_Amount = request.getParameter("vnp_Amount");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_BankCode = request.getParameter("vnp_BankCode");
            
            System.out.println("=== VNPay Return ===");
            System.out.println("Response Code: " + vnp_ResponseCode);
            System.out.println("TxnRef (Booking Code): " + vnp_TxnRef);
            System.out.println("Amount: " + vnp_Amount);
            System.out.println("Transaction No: " + vnp_TransactionNo);
            System.out.println("Bank Code: " + vnp_BankCode);
            
            // Find booking by booking code
            Optional<Booking> bookingOpt = bookingService.findByBookingCode(vnp_TxnRef);
            if (!bookingOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
                return "redirect:/booking/my-bookings";
            }
            
            Booking booking = bookingOpt.get();
            
            // Get vehicle info
            Vehicle vehicle = booking.getVehicle();
            
            // Check response code
            if ("00".equals(vnp_ResponseCode)) {
                // Payment successful
                System.out.println("✅ Payment successful!");
                
                // Update payment record
                Optional<Payment> paymentOpt = paymentService.findByBookingId(booking.getBookingId());
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                    payment.setTransactionId(vnp_TransactionNo);
                    payment.setPaymentDate(java.time.LocalDateTime.now());
                    paymentService.savePayment(payment);
                }
                
                // Update booking - Chờ Owner duyệt
                booking.setStatus(Booking.BookingStatus.Pending); // Chờ Owner duyệt
                bookingService.updateBooking(booking);
                
                // Gửi thông báo cho Staff (chỉ thông báo, không cần xác nhận)
                notificationService.createNotificationForAllStaff(
                    "Đơn đặt xe " + booking.getBookingCode() + " đã thanh toán thành công - Số tiền: " + 
                    String.format("%,.0f", booking.getTotalAmount()) + " ₫",
                    booking.getBookingId(),
                    "PAYMENT_SUCCESS"
                );
                
                // Tự động gửi request đến Owner để duyệt
                notificationService.createNotificationForAllAdmins(
                    "Đơn đặt xe mới " + booking.getBookingCode() + " đã thanh toán thành công. Vui lòng duyệt đơn.",
                    booking.getBookingId(),
                    "BOOKING_APPROVAL"
                );
                
                model.addAttribute("success", true);
                model.addAttribute("message", "Thanh toán VNPay thành công!");
                model.addAttribute("transactionNo", vnp_TransactionNo);
            } else {
                // Payment failed
                System.out.println("❌ Payment failed! Code: " + vnp_ResponseCode);
                
                model.addAttribute("error", true);
                model.addAttribute("message", "Thanh toán không thành công! Mã lỗi: " + vnp_ResponseCode);
            }
            
            // Add all necessary data to model
            model.addAttribute("booking", booking);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("vnpTxnRef", vnp_TxnRef); // Booking code for display
            model.addAttribute("totalAmount", booking.getTotalAmount());
            model.addAttribute("vnpAmount", vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : booking.getTotalAmount());
            model.addAttribute("vnpBankCode", vnp_BankCode != null ? vnp_BankCode : "N/A");
            model.addAttribute("vnpTransactionNo", vnp_TransactionNo != null ? vnp_TransactionNo : "N/A");
            model.addAttribute("currentUser", session.getAttribute("currentUser"));
            
            return "customer/payment-result";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/booking/my-bookings";
        }
    }
    
    /**
     * VNPay IPN callback (for server-to-server notification)
     */
    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public String vnpayIPN(HttpServletRequest request) {
        try {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            
            System.out.println("=== VNPay IPN ===");
            System.out.println("Response Code: " + vnp_ResponseCode);
            System.out.println("TxnRef: " + vnp_TxnRef);
            System.out.println("Transaction No: " + vnp_TransactionNo);
            
            if ("00".equals(vnp_ResponseCode)) {
                // Find and update booking
                Optional<Booking> bookingOpt = bookingService.findByBookingCode(vnp_TxnRef);
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    
                    // Update payment
                    Optional<Payment> paymentOpt = paymentService.findByBookingId(booking.getBookingId());
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                        payment.setTransactionId(vnp_TransactionNo);
                        payment.setPaymentDate(java.time.LocalDateTime.now());
                        paymentService.savePayment(payment);
                    }
                    
                    // Update booking - Giữ trạng thái Pending, chờ Owner duyệt
                    booking.setStatus(Booking.BookingStatus.Pending);
                    bookingService.updateBooking(booking);
                    
                    return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
                }
            }
            
            return "{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"RspCode\":\"99\",\"Message\":\"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Show QR payment result
     */
    @GetMapping("/qr-result")
    public String showQRResult(@RequestParam String bookingId,
                              @RequestParam String status,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt xe!");
            return "redirect:/booking/my-bookings";
        }

        Booking booking = bookingOpt.get();

        if ("success".equals(status)) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
        } else {
            model.addAttribute("error", "Thanh toán không thành công!");
            model.addAttribute("message", "Vui lòng thử lại hoặc chọn phương thức khác.");
        }

        model.addAttribute("booking", booking);
        model.addAttribute("currentUser", user);

        return "customer/payment-result";
    }
}
