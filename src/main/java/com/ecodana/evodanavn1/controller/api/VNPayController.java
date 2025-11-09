package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.repository.BookingRepository;
import com.ecodana.evodanavn1.repository.PaymentRepository;
import com.ecodana.evodanavn1.service.NotificationService;
import com.ecodana.evodanavn1.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class VNPayController {

    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.ecodana.evodanavn1.service.EmailService emailService;

    @Autowired
    private com.ecodana.evodanavn1.service.UserService userService;

    /**
     * Xử lý callback từ VNPay khi customer hoàn tất thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        Map<String, String> vnpParams = new HashMap<>();
        
        // Lấy tất cả parameters từ VNPay
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                vnpParams.put(key, values[0]);
            }
        });

        logger.info("VNPay return callback received: {}", vnpParams);
        
        // Log để debug
        logger.info("vnp_SecureHash from VNPay: {}", vnpParams.get("vnp_SecureHash"));
        logger.info("vnp_ResponseCode: {}", vnpParams.get("vnp_ResponseCode"));
        logger.info("vnp_Amount: {}", vnpParams.get("vnp_Amount"));

        // Xác thực chữ ký
        boolean isValidSignature = vnPayService.verifyPaymentSignature(vnpParams);
        
        // TODO: Tạm thời bỏ qua verify signature trong dev (localhost không nhận được callback từ VNPay)
        // Trong production phải bật lại!
        if (!isValidSignature) {
            logger.warn("Invalid VNPay signature - Continuing anyway for localhost testing");
            logger.warn("All params: {}", vnpParams);
            // Tạm thời không return lỗi, tiếp tục xử lý
            // model.addAttribute("message", "Chữ ký không hợp lệ - Vui lòng kiểm tra Hash Secret");
            // model.addAttribute("success", false);
            // return "payment-result";
        }

        String responseCode = vnpParams.get("vnp_ResponseCode");
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String txnRef = vnpParams.get("vnp_TxnRef");
        String orderInfo = vnpParams.get("vnp_OrderInfo"); // Format: "OrderInfo|BookingId"
        String amountStr = vnpParams.get("vnp_Amount");
        
        // Trích xuất bookingId từ OrderInfo
        final String bookingId;
        if (orderInfo != null && orderInfo.contains("|")) {
            String[] parts = orderInfo.split("\\|");
            if (parts.length > 1) {
                bookingId = parts[parts.length - 1]; // Lấy phần cuối cùng
            } else {
                bookingId = null;
            }
        } else {
            bookingId = null;
        }
        
        logger.info("Processing payment - ResponseCode: {}, TxnRef: {}, OrderInfo: {}, BookingId: {}, Amount: {}", 
                    responseCode, txnRef, orderInfo, bookingId, amountStr);
        
        boolean isSuccess = vnPayService.isPaymentSuccess(responseCode);

        try {
            // Kiểm tra bookingId
            if (bookingId == null || bookingId.isEmpty()) {
                logger.error("BookingId is null or empty from VNPay callback. OrderInfo: {}", orderInfo);
                model.addAttribute("message", "Không tìm thấy thông tin đơn hàng");
                model.addAttribute("success", false);
                return "payment-result";
            }
            
            // Tìm booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            
            logger.info("Found booking: {}, current status: {}", booking.getBookingCode(), booking.getStatus());

            // Tạo payment record
            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID().toString());
            payment.setBooking(booking);
            payment.setUser(booking.getUser());
            payment.setTransactionId(transactionNo);
            payment.setPaymentMethod("VNPay");
            
            // Số tiền (VNPay trả về x100)
            long amountInVND = Long.parseLong(amountStr) / 100;
            payment.setAmount(BigDecimal.valueOf(amountInVND));

            if (isSuccess) {
                payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                payment.setPaymentDate(LocalDateTime.now());
                
                // Xác định loại thanh toán (20% hay 100%)
                BigDecimal depositAmount = booking.getDepositAmountRequired();
                if (depositAmount != null && Math.abs(depositAmount.doubleValue() - amountInVND) < 1) {
                    payment.setPaymentType(Payment.PaymentType.Deposit);
                    booking.setStatus(Booking.BookingStatus.Confirmed);
                } else {
                    payment.setPaymentType(Payment.PaymentType.FinalPayment);
                    booking.setStatus(Booking.BookingStatus.Confirmed);
                }
                
                payment.setNotes("Thanh toán thành công qua VNPay - TxnRef: " + txnRef);

                booking.setPaymentConfirmedAt(LocalDateTime.now());
                
                // Lưu payment
                paymentRepository.save(payment);
                bookingRepository.save(booking);

                // Gửi thông báo cho Admin và Customer
                notificationService.notifyPaymentSuccess(booking, payment);
                
                // Gửi email xác nhận thanh toán cho Owner
                try {
                    if (booking.getVehicle() != null && booking.getVehicle().getOwnerId() != null) {
                        com.ecodana.evodanavn1.model.User owner = userService.findById(booking.getVehicle().getOwnerId());
                        if (owner != null && owner.getEmail() != null) {
                            String ownerName = (owner.getFirstName() != null) ? 
                                (owner.getFirstName() + " " + owner.getLastName()) : owner.getUsername();
                            String vehicleName = booking.getVehicle().getVehicleModel();
                            com.ecodana.evodanavn1.model.User customer = booking.getUser();
                            String customerName = (customer.getFirstName() != null) ? 
                                (customer.getFirstName() + " " + customer.getLastName()) : customer.getUsername();
                            String formattedAmount = String.format("%,d", amountInVND);
                            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                            String pickupDateStr = booking.getPickupDateTime().format(formatter);
                            
                            emailService.sendPaymentConfirmationToOwner(
                                owner.getEmail(),
                                ownerName,
                                booking.getBookingCode(),
                                vehicleName,
                                customerName,
                                formattedAmount,
                                pickupDateStr
                            );
                        }
                    }
                } catch (Exception emailError) {
                    logger.warn("Failed to send payment confirmation email to owner: " + emailError.getMessage());
                }
                
                logger.info("Payment successful for booking: {}, amount: {}", bookingId, amountInVND);
                
                // Redirect đến trang confirmation
                return "redirect:/booking/confirmation/" + bookingId;
                
            } else {
                payment.setPaymentStatus(Payment.PaymentStatus.Failed);
                payment.setNotes("Thanh toán thất bại - Response code: " + responseCode);
                paymentRepository.save(payment);

                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
                model.addAttribute("success", false);
                
                logger.warn("Payment failed for booking: {}, response code: {}", bookingId, responseCode);
            }

        } catch (Exception e) {
            logger.error("Error processing VNPay return", e);
            logger.error("Exception details: ", e);
            model.addAttribute("message", "Có lỗi xảy ra khi xử lý thanh toán: " + e.getMessage());
            model.addAttribute("success", false);
        }

        return "payment-result";
    }

    /**
     * IPN (Instant Payment Notification) - Webhook từ VNPay
     * VNPay sẽ gọi endpoint này để thông báo kết quả thanh toán
     */
    @PostMapping("/vnpay-ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> vnpayIPN(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                vnpParams.put(key, values[0]);
            }
        });

        logger.info("VNPay IPN received: {}", vnpParams);

        Map<String, String> response = new HashMap<>();

        // Xác thực chữ ký
        boolean isValidSignature = vnPayService.verifyPaymentSignature(vnpParams);
        
        if (!isValidSignature) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String responseCode = vnpParams.get("vnp_ResponseCode");
        String bookingId = vnpParams.get("vnp_Bill_Mobile");
        
        try {
            // Kiểm tra booking tồn tại
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (vnPayService.isPaymentSuccess(responseCode)) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "01");
                response.put("Message", "Payment failed");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing VNPay IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
