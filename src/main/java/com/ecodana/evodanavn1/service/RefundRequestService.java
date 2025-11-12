package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.*;
import com.ecodana.evodanavn1.repository.RefundRequestRepository;
import com.ecodana.evodanavn1.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefundRequestService {

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public RefundRequest createRefundRequest(Booking booking, User user, String cancelReason) {
        // Check if refund request already exists
        Optional<RefundRequest> existingRequest = refundRequestRepository.findByBookingBookingId(booking.getBookingId());
        if (existingRequest.isPresent()) {
            throw new IllegalStateException("Yêu cầu hoàn tiền cho đơn hàng này đã tồn tại!");
        }

        // Get user's default bank account
        Optional<BankAccount> defaultBankAccount = bankAccountService.getDefaultBankAccount(user.getId());
        if (defaultBankAccount.isEmpty()) {
            throw new IllegalStateException("Bạn cần thêm thông tin tài khoản ngân hàng trước khi yêu cầu hoàn tiền!");
        }

        // Calculate refund amount
        BigDecimal refundAmount = calculateRefundAmount(booking);

        // Check if within 2 hours of payment
        boolean isWithinTwoHours = isWithinTwoHoursOfPayment(booking);

        // Create refund request
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setRefundRequestId(UUID.randomUUID().toString());
        refundRequest.setBooking(booking);
        refundRequest.setUser(user);
        refundRequest.setBankAccount(defaultBankAccount.get());
        refundRequest.setRefundAmount(refundAmount);
        refundRequest.setCancelReason(cancelReason);
        refundRequest.setStatus(RefundRequest.RefundStatus.Pending);
        refundRequest.setWithinTwoHours(isWithinTwoHours);

        RefundRequest savedRequest = refundRequestRepository.save(refundRequest);

        // Send notification to admin
        notifyAdminOfRefundRequest(savedRequest);

        return savedRequest;
    }

    private BigDecimal calculateRefundAmount(Booking booking) {
        // Get total paid amount
        List<Payment> payments = paymentRepository.findByBookingId(booking.getBookingId());
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.Completed)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime paymentTime = booking.getPaymentConfirmedAt();
        LocalDateTime tripStartTime = booking.getPickupDateTime();

        if (paymentTime == null) {
            return totalPaid; // Fallback
        }

        long hoursSincePayment = Duration.between(paymentTime, now).toHours();
        long daysBeforeTrip = Duration.between(now, tripStartTime).toDays();

        BigDecimal tripValue = booking.getTotalAmount();
        BigDecimal refundAmount;

        if (hoursSincePayment < 2) {
            // Hủy trong 2 giờ -> Hoàn 100%
            refundAmount = totalPaid;
        } else if (daysBeforeTrip >= 7) {
            // Hủy trước 7 ngày -> Phí hủy 10%
            BigDecimal penalty = tripValue.multiply(new BigDecimal("0.10"));
            refundAmount = totalPaid.subtract(penalty);
        } else {
            // Hủy trong 7 ngày -> Phí hủy 40%
            BigDecimal penalty = tripValue.multiply(new BigDecimal("0.40"));
            refundAmount = totalPaid.subtract(penalty);
        }

        return refundAmount.max(BigDecimal.ZERO);
    }

    private boolean isWithinTwoHoursOfPayment(Booking booking) {
        if (booking.getPaymentConfirmedAt() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long hoursSincePayment = Duration.between(booking.getPaymentConfirmedAt(), now).toHours();
        return hoursSincePayment < 2;
    }

    private void notifyAdminOfRefundRequest(RefundRequest refundRequest) {
        String message = String.format(
            "Yêu cầu hoàn tiền mới từ %s cho đơn hàng #%s. Số tiền: %s VNĐ. %s",
            refundRequest.getUser().getUsername(),
            refundRequest.getBooking().getBookingCode(),
            refundRequest.getRefundAmount(),
            refundRequest.isWithinTwoHours() ? "⚡ KHẨN CẤP - Trong vòng 2 giờ!" : ""
        );

        notificationService.createNotificationForAllAdmins(
            message,
            refundRequest.getRefundRequestId(),
            "REFUND_REQUEST"
        );
    }

    public List<RefundRequest> getRefundRequestsByUserId(String userId) {
        return refundRequestRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    public List<RefundRequest> getPendingRefundRequests() {
        return refundRequestRepository.findPendingRequestsOrderByCreatedDate();
    }

    public List<RefundRequest> getUrgentRefundRequests() {
        return refundRequestRepository.findUrgentPendingRequests();
    }

    public long countPendingRequests() {
        return refundRequestRepository.countPendingRequests();
    }

    public Optional<RefundRequest> getRefundRequestById(String refundRequestId) {
        return refundRequestRepository.findById(refundRequestId);
    }

    @Transactional
    public void approveRefundRequest(String refundRequestId, String adminUserId, String adminNotes) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền"));

        refundRequest.setStatus(RefundRequest.RefundStatus.Approved);
        refundRequest.setProcessedBy(adminUserId);
        refundRequest.setProcessedDate(LocalDateTime.now());
        refundRequest.setAdminNotes(adminNotes);

        refundRequestRepository.save(refundRequest);

        // Update booking status
        Booking booking = refundRequest.getBooking();
        booking.setStatus(Booking.BookingStatus.Cancelled);
        booking.setCancelReason("Admin đã duyệt yêu cầu hoàn tiền: " + adminNotes);

        // Notify customer
        notificationService.createNotification(
            refundRequest.getUser().getId(),
            "Yêu cầu hoàn tiền đã được duyệt! Số tiền " + refundRequest.getRefundAmount() + " VNĐ sẽ được chuyển vào tài khoản của bạn trong 1-3 ngày làm việc.",
            refundRequestId,
            "REFUND_APPROVED"
        );
    }

    @Transactional
    public void rejectRefundRequest(String refundRequestId, String adminUserId, String adminNotes) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu hoàn tiền"));

        refundRequest.setStatus(RefundRequest.RefundStatus.Rejected);
        refundRequest.setProcessedBy(adminUserId);
        refundRequest.setProcessedDate(LocalDateTime.now());
        refundRequest.setAdminNotes(adminNotes);

        refundRequestRepository.save(refundRequest);

        // Notify customer
        notificationService.createNotification(
            refundRequest.getUser().getId(),
            "Yêu cầu hoàn tiền đã bị từ chối. Lý do: " + adminNotes,
            refundRequestId,
            "REFUND_REJECTED"
        );
    }
}
