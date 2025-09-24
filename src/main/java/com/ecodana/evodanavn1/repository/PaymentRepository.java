package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    /**
     * Find payments by booking ID
     * @param bookingId the booking ID
     * @return list of payments for the booking
     */
    List<Payment> findByBookingId(String bookingId);
    
    /**
     * Find payments by user ID
     * @param userId the user ID
     * @return list of payments by the user
     */
    List<Payment> findByUserId(String userId);
    
    /**
     * Find payments by payment status
     * @param paymentStatus the payment status
     * @return list of payments with the status
     */
    List<Payment> findByPaymentStatus(String paymentStatus);
    
    /**
     * Find payments by payment method
     * @param paymentMethod the payment method
     * @return list of payments with the method
     */
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    /**
     * Find payments by payment type
     * @param paymentType the payment type
     * @return list of payments with the type
     */
    List<Payment> findByPaymentType(String paymentType);
    
    /**
     * Find payment by transaction ID
     * @param transactionId the transaction ID
     * @return optional payment
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Check if payment exists by transaction ID
     * @param transactionId the transaction ID
     * @return true if exists, false otherwise
     */
    boolean existsByTransactionId(String transactionId);
    
    /**
     * Find successful payments
     * @return list of successful payments
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'Paid'")
    List<Payment> findSuccessfulPayments();
    
    /**
     * Find pending payments
     * @return list of pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'Pending'")
    List<Payment> findPendingPayments();
}
