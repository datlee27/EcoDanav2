package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.Payment;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Create a new payment record
     */
    @Transactional
    public Payment createPayment(Booking booking, User user, BigDecimal amount, String paymentMethod, 
                                 Payment.PaymentType paymentType, String transactionId) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentType(paymentType);
        payment.setPaymentStatus(Payment.PaymentStatus.Pending);
        payment.setTransactionId(transactionId);
        payment.setCreatedDate(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    /**
     * Update payment status
     */
    @Transactional
    public Payment updatePaymentStatus(String paymentId, Payment.PaymentStatus status, String notes) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setPaymentStatus(status);
            if (notes != null) {
                payment.setNotes(notes);
            }
            if (status == Payment.PaymentStatus.Completed && payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            return paymentRepository.save(payment);
        }
        return null;
    }

    /**
     * Find payment by transaction ID
     */
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findAll().stream()
                .filter(p -> transactionId.equals(p.getTransactionId()))
                .findFirst();
    }

    /**
     * Find payment by booking ID
     */
    public Optional<Payment> findByBookingId(String bookingId) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getBooking() != null && p.getBooking().getBookingId().equals(bookingId))
                .findFirst();
    }

    /**
     * Find payment by ID
     */
    public Optional<Payment> findById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * Get all payments for a booking
     */
    public List<Payment> getPaymentsByBooking(Booking booking) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getBooking() != null && p.getBooking().getBookingId().equals(booking.getBookingId()))
                .toList();
    }

    /**
     * Get all payments for a user
     */
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()))
                .toList();
    }

    /**
     * Save payment
     */
    @Transactional
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Check if booking has completed payment
     */
    public boolean hasCompletedPayment(Booking booking) {
        return paymentRepository.findAll().stream()
                .anyMatch(p -> p.getBooking() != null 
                        && p.getBooking().getBookingId().equals(booking.getBookingId())
                        && p.getPaymentStatus() == Payment.PaymentStatus.Completed);
    }
}
