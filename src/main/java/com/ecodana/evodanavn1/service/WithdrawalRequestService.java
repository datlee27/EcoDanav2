package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Notification;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.WithdrawalRequest;
import com.ecodana.evodanavn1.repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawalRequestService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final UserService userService; // Để cập nhật số dư của owner
    private final PaymentService paymentService; // Để tính toán số dư khả dụng
    private final NotificationService notificationService; // Để gửi thông báo

    @Autowired
    public WithdrawalRequestService(WithdrawalRequestRepository withdrawalRequestRepository, UserService userService, PaymentService paymentService, NotificationService notificationService) {
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.userService = userService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    @Transactional
    public WithdrawalRequest createWithdrawalRequest(User owner, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0.");
        }

        // Lấy thông tin owner mới nhất để đảm bảo số dư chính xác
        User currentOwner = userService.findById(owner.getId());
        if (currentOwner == null) {
            throw new IllegalArgumentException("Owner không tồn tại.");
        }

        // Tính toán số dư khả dụng thực tế của owner
        BigDecimal netRevenue = paymentService.calculateNetRevenueForOwner(owner.getId());
        BigDecimal pendingWithdrawals = getPendingWithdrawalRequestsForOwner(owner.getId()).stream()
                .map(WithdrawalRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal availableBalance = netRevenue.subtract(pendingWithdrawals);

        if (amount.compareTo(availableBalance) > 0) {
            throw new IllegalArgumentException("Số tiền rút vượt quá số dư khả dụng của bạn (" + availableBalance + " ₫).");
        }

        WithdrawalRequest request = new WithdrawalRequest();
        request.setOwner(currentOwner);
        request.setAmount(amount);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(WithdrawalRequest.WithdrawalStatus.PENDING);
        
        WithdrawalRequest savedRequest = withdrawalRequestRepository.save(request);

        // Gửi thông báo cho Admin về yêu cầu rút tiền mới
        notificationService.createNotificationForAllAdmins(
                "Chủ xe " + currentOwner.getUsername() + " đã gửi yêu cầu rút " + amount + " ₫.",
                savedRequest.getId(),
                "WITHDRAWAL_REQUEST"
        );

        return savedRequest;
    }

    public List<WithdrawalRequest> getPendingWithdrawalRequestsForOwner(String ownerId) {
        return withdrawalRequestRepository.findByOwnerIdAndStatus(ownerId, WithdrawalRequest.WithdrawalStatus.PENDING);
    }

    public List<WithdrawalRequest> getAllWithdrawalRequestsForOwner(String ownerId) {
        return withdrawalRequestRepository.findByOwnerId(ownerId);
    }

    public List<WithdrawalRequest> getAllPendingWithdrawalRequests() {
        return withdrawalRequestRepository.findByStatus(WithdrawalRequest.WithdrawalStatus.PENDING);
    }

    public WithdrawalRequest getWithdrawalRequestById(String requestId) {
        return withdrawalRequestRepository.findById(requestId).orElse(null);
    }

    @Transactional
    public WithdrawalRequest approveWithdrawalRequest(String requestId, String adminNotes) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu rút tiền không tồn tại."));

        if (request.getStatus() != WithdrawalRequest.WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu rút tiền này không ở trạng thái PENDING.");
        }

        // Trừ tiền từ số dư của owner
        User owner = request.getOwner();
        userService.deductBalance(owner.getId(), request.getAmount()); // Trừ tiền từ balance của user

        request.setStatus(WithdrawalRequest.WithdrawalStatus.APPROVED);
        request.setAdminNotes(adminNotes);
        request.setProcessedDate(LocalDateTime.now());
        WithdrawalRequest approvedRequest = withdrawalRequestRepository.save(request);

        // Gửi thông báo cho owner
        notificationService.createNotification(
                owner.getId(),
                "Yêu cầu rút tiền " + request.getAmount() + " ₫ của bạn đã được duyệt và xử lý.",
                approvedRequest.getId(),
                "WITHDRAWAL_STATUS"
        );

        return approvedRequest;
    }

    @Transactional
    public WithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNotes) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu rút tiền không tồn tại."));

        if (request.getStatus() != WithdrawalRequest.WithdrawalStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu rút tiền này không ở trạng thái PENDING.");
        }

        request.setStatus(WithdrawalRequest.WithdrawalStatus.REJECTED);
        request.setAdminNotes(adminNotes);
        request.setProcessedDate(LocalDateTime.now());
        WithdrawalRequest rejectedRequest = withdrawalRequestRepository.save(request);

        // Gửi thông báo cho owner
        notificationService.createNotification(
                request.getOwner().getId(),
                "Yêu cầu rút tiền " + request.getAmount() + " ₫ của bạn đã bị từ chối. Lý do: " + adminNotes,
                rejectedRequest.getId(),
                "WITHDRAWAL_STATUS"
        );

        return rejectedRequest;
    }

    public List<WithdrawalRequest> findByStatus(WithdrawalRequest.WithdrawalStatus status) {
        return withdrawalRequestRepository.findByStatus(status);
    }
}
