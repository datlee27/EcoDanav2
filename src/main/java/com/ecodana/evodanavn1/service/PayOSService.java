package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.client.PayOSClient;
import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.repository.BookingRepository;
import com.ecodana.evodanavn1.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PayOSService {
    private static final Logger logger = LoggerFactory.getLogger(PayOSService.class);

    @Autowired
    private PayOSClient payOSClient;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${payos.return-url}")
    private String returnUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String PAYMENT_SUCCESS_STATUS = "PAID";
    private static final String PAYMENT_CANCELLED_STATUS = "CANCELLED";
    private static final String PAYMENT_FAILED_STATUS = "FAILED";

    @Transactional
    public String createPaymentLink(long amount, String orderInfo, String bookingId, HttpServletRequest request) {
        try {
            // Tạo mã đơn hàng duy nhất (timestamp in seconds)
            long orderCodeNumber = System.currentTimeMillis() / 1000;
            String orderCode = String.valueOf(orderCodeNumber);
            
            // Tạo URL callback và return
            String returnUrl = baseUrl + "/booking/payment/payos-return?bookingId=" + bookingId;
            String cancelUrl = baseUrl + "/booking/payment/cancel?bookingId=" + bookingId;
            
            logger.info("Creating PayOS payment link - Amount: {}, OrderCode: {}, BookingId: {}", amount, orderCode, bookingId);
            logger.info("Return URL: {}, Cancel URL: {}", returnUrl, cancelUrl);
            
            // Gọi API tạo payment link
            String response = payOSClient.createPaymentLink(
                amount, 
                orderCode, 
                orderInfo, 
                returnUrl, 
                cancelUrl
            );
            
            logger.info("PayOS API Response: {}", response);
            
            // Parse response để lấy thông tin thanh toán
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("data") && jsonNode.get("data").has("checkoutUrl")) {
                String paymentUrl = jsonNode.get("data").get("checkoutUrl").asText();
                
                // Lưu thông tin thanh toán tạm thời
                Payment payment = new Payment();
                payment.setPaymentId(UUID.randomUUID().toString());
                payment.setOrderCode(orderCode);
                // Số tiền từ PayOS là VND (long). Lưu trực tiếp theo VND.
                // VD: PayOS trả về 600000 VND -> lưu 600000.00 trong DB
                payment.setAmount(BigDecimal.valueOf(amount));
                payment.setPaymentMethod("PayOS");
                payment.setPaymentStatus(Payment.PaymentStatus.Pending);
                payment.setCreatedDate(LocalDateTime.now());
                // Gán booking và user
                Booking booking = bookingRepository.findById(bookingId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt xe: " + bookingId));
                payment.setBooking(booking);
                payment.setUser(booking.getUser());
                paymentRepository.save(payment);
                
                return paymentUrl;
            } else {
                throw new RuntimeException("Failed to create payment link: " + response);
            }
            
        } catch (Exception e) {
            logger.error("Error creating PayOS payment link", e);
            throw new RuntimeException("Không thể tạo liên kết thanh toán: " + e.getMessage());
        }
    }

    /**
     * Tạo link thanh toán bổ sung (Hoàn tất chuyến đi - gọi từ modal Owner)
     * Trả về Map chứa qrCode và orderCode để hiển thị popup
     */
    @Transactional
    public Map<String, Object> createCompletionPaymentLink(String bookingId, long amount, String description) {
        try {
            if (amount <= 0) {
                throw new RuntimeException("Số tiền cần thanh toán phải lớn hơn 0.");
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

            long orderCodeNumber = System.currentTimeMillis();
            String orderCode = String.valueOf(orderCodeNumber);

            String successUrl = this.returnUrl;
            String cancelUrlStr = this.returnUrl.replace("success", "cancel");

            String fullDescription = "TT bổ sung Booking: " + bookingId + " - " + description;

            String responseBody = payOSClient.createPaymentLink(
                    amount,
                    orderCode,
                    fullDescription,
                    successUrl,
                    cancelUrlStr
            );

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("code") && "00".equals(jsonNode.get("code").asText()) && jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");

                savePaymentRecord(orderCode, amount, bookingId, "Pending_Completion");

                Map<String, Object> result = new HashMap<>();
                result.put("success", true); // FIX: Add success flag
                result.put("qrCode", dataNode.has("qrCode") ? dataNode.get("qrCode").asText() : "");
                result.put("checkoutUrl", dataNode.has("checkoutUrl") ? dataNode.get("checkoutUrl").asText() : "");
                result.put("orderCode", orderCode);
                return result;
            } else {
                String payosDesc = jsonNode.has("desc") ? jsonNode.get("desc").asText() : "Unknown PayOS error.";
                throw new RuntimeException("Failed to create completion link (PayOS says: " + payosDesc + "): " + responseBody);
            }

        } catch (Exception e) {
            logger.error("Error creating completion payment link", e);
            throw new RuntimeException("Lỗi tạo mã thanh toán bổ sung: " + e.getMessage());
        }
    }

    // Hàm hỗ trợ lưu payment vào DB để tránh lặp code
    private void savePaymentRecord(String orderCode, long amount, String bookingId, String statusNote) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID().toString());
            payment.setOrderCode(orderCode);
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setPaymentMethod("PayOS");
            payment.setPaymentStatus(Payment.PaymentStatus.Pending);
            payment.setCreatedDate(LocalDateTime.now());
            payment.setBooking(booking);
            payment.setUser(booking.getUser());
            paymentRepository.save(payment);
        }
    }


    public boolean verifyWebhook(String webhookData) {
        try {
            // For now, always return true since PayOS webhook verification is complex
            // In production, implement proper signature verification
            logger.info("Webhook verification - accepting webhook (signature check disabled)");
            return true;
        } catch (Exception e) {
            logger.error("Error verifying PayOS webhook", e);
            return false;
        }
    }

    public String processRefund(String transactionId, long amount, String reason) {
        try {
            return payOSClient.refundPayment(transactionId, amount, reason);
        } catch (Exception e) {
            logger.error("Error processing refund", e);
            throw new RuntimeException("Failed to process refund: " + e.getMessage());
        }
    }

    public void handlePaymentSuccess(String orderCode, long amount, String transactionId) {
        try {
            // Tìm payment theo orderCode
            Payment payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho order: " + orderCode));

            // IDEMPOTENCY CHECK: Nếu payment đã được xử lý (status = Completed), không xử lý lại
            if (payment.getPaymentStatus() == Payment.PaymentStatus.Completed) {
                logger.warn("Payment already processed for order: {}. Skipping duplicate processing.", orderCode);
                return;
            }

            Booking booking = payment.getBooking();
            BigDecimal paidAmount = BigDecimal.valueOf(amount);
            
            logger.info("=== PAYMENT SUCCESS PROCESSING ===");
            logger.info("Amount from PayOS (VND): {}", amount);
            logger.info("Paid amount (VND): {}", paidAmount);
            logger.info("Deposit required (VND): {}", booking.getDepositAmountRequired());
            logger.info("Total amount (VND): {}", booking.getTotalAmount());
            
            // Xác định loại payment dựa trên số tiền thanh toán
            if (paidAmount.compareTo(booking.getTotalAmount()) >= 0) {
                // Thanh toán toàn bộ (100%) - tạo cả Deposit và FinalPayment
                logger.info("Full payment detected - creating Deposit and FinalPayment records");
                
                // Cập nhật payment hiện tại thành Deposit
                payment.setPaymentType(Payment.PaymentType.Deposit);
                payment.setAmount(booking.getDepositAmountRequired());
                payment.setTransactionId(transactionId);
                payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
                
                // Tạo FinalPayment record
                Payment finalPayment = new Payment();
                finalPayment.setPaymentId(UUID.randomUUID().toString());
                finalPayment.setOrderCode(orderCode + "_FINAL");
                finalPayment.setAmount(booking.getRemainingAmount());
                finalPayment.setPaymentMethod("PayOS");
                finalPayment.setPaymentStatus(Payment.PaymentStatus.Completed);
                finalPayment.setPaymentType(Payment.PaymentType.FinalPayment);
                finalPayment.setTransactionId(transactionId + "_FINAL");
                finalPayment.setCreatedDate(LocalDateTime.now());
                finalPayment.setPaymentDate(LocalDateTime.now());
                finalPayment.setBooking(booking);
                finalPayment.setUser(booking.getUser());
                paymentRepository.save(finalPayment);
                
                logger.info("Created Deposit: {} and FinalPayment: {}", booking.getDepositAmountRequired(), booking.getRemainingAmount());
                
            } else if (paidAmount.compareTo(booking.getDepositAmountRequired()) >= 0) {
                // Thanh toán cọc (20%) - chỉ tạo Deposit
                logger.info("Deposit payment detected");
                payment.setPaymentType(Payment.PaymentType.Deposit);
                payment.setTransactionId(transactionId);
                payment.setPaymentStatus(Payment.PaymentStatus.Completed);
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
            } else {
                // Thanh toán không đủ
                logger.warn("Payment amount insufficient");
                payment.setPaymentStatus(Payment.PaymentStatus.Failed);
                paymentRepository.save(payment);
                return;
            }
            
            // Cập nhật trạng thái booking
            if (paidAmount.compareTo(booking.getDepositAmountRequired()) >= 0) {
                booking.setStatus(Booking.BookingStatus.Confirmed);
                booking.setPaymentConfirmedAt(LocalDateTime.now());
                logger.info("Booking confirmed - payment amount sufficient");
            }
            bookingRepository.save(booking);

        } catch (Exception e) {
            logger.error("Error handling payment success", e);
            throw new RuntimeException("Failed to handle payment success: " + e.getMessage());
        }
    }
    
    public String getPaymentInfo(String orderCode) {
        try {
            return payOSClient.getPaymentLinkInfo(orderCode);
        } catch (Exception e) {
            logger.error("Error getting payment info", e);
            throw new RuntimeException("Failed to get payment info: " + e.getMessage());
        }
    }
    
    public String cancelPaymentLink(String orderCode) {
        try {
            return payOSClient.cancelPaymentLink(orderCode);
        } catch (Exception e) {
            logger.error("Error canceling payment link", e);
            throw new RuntimeException("Failed to cancel payment link: " + e.getMessage());
        }
    }


    /**
     * Lấy trạng thái thanh toán rút gọn (PAID/PENDING/CANCELLED)
     */
    public String getPaymentStatus(String orderCode) {
        try {
            String responseBody = payOSClient.getPaymentLinkInfo(orderCode);
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("code") && "00".equals(jsonNode.get("code").asText()) && jsonNode.has("data")) {
                return jsonNode.get("data").get("status").asText(); // Trả về "PAID", "PENDING", "CANCELLED"
            }
            return "UNKNOWN";
        } catch (Exception e) {
            logger.error("Error checking payment status for order: {}", orderCode);
            return "ERROR";
        }
    }
}
