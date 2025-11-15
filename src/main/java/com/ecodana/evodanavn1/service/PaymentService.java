package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Lấy tất cả các khoản thanh toán liên quan đến các xe của một chủ sở hữu.
     * @param ownerId ID của chủ xe
     * @return Danh sách các khoản thanh toán
     */
    public List<Payment> getPaymentsForOwner(String ownerId) {
        return paymentRepository.findPaymentsByVehicleOwnerId(ownerId);
    }

    /**
     * Tính toán các chỉ số thống kê thanh toán cho chủ xe.
     * Total Revenue: Tổng tiền từ các payment đã 'Completed' (không phụ thuộc vào booking status).
     * Net Revenue: Total Revenue trừ đi các khoản đã 'Refunded'.
     * @param ownerId ID của chủ xe
     * @return Map chứa totalRevenue và netRevenue
     */
    public Map<String, BigDecimal> getOwnerPaymentStatistics(String ownerId) {
        List<Payment> ownerPayments = getPaymentsForOwner(ownerId);

        // Doanh thu tổng là tổng các khoản thanh toán có payment status = 'Completed'
        // Bao gồm cả Deposit và FinalPayment
        BigDecimal totalRevenue = ownerPayments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.Completed)
                .filter(p -> p.getPaymentType() == Payment.PaymentType.Deposit || 
                           p.getPaymentType() == Payment.PaymentType.FinalPayment)
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tiền hoàn lại là tổng các khoản thanh toán có trạng thái 'Refunded'.
        BigDecimal totalRefunds = ownerPayments.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.Refunded)
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .map(BigDecimal::abs) // Đảm bảo giá trị dương
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Doanh thu thực nhận = Doanh thu tổng - Tiền hoàn lại.
        BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);

        return Map.of("totalRevenue", totalRevenue, "netRevenue", netRevenue);
    }

    /**
     * Tính toán tổng doanh thu thực nhận (net revenue) cho một chủ xe.
     * Đây là số tiền mà chủ xe thực sự nhận được sau khi trừ đi các khoản hoàn tiền.
     * @param ownerId ID của chủ xe
     * @return Tổng doanh thu thực nhận
     */
    public BigDecimal calculateNetRevenueForOwner(String ownerId) {
        Map<String, BigDecimal> stats = getOwnerPaymentStatistics(ownerId);
        return stats.getOrDefault("netRevenue", BigDecimal.ZERO);
    }
}
