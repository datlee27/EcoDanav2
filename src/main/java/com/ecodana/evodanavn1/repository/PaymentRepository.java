package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId")
    List<Payment> findByBookingId(@Param("bookingId") String bookingId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId")
    List<Payment> findByUserId(@Param("userId") String userId);

    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findByPaymentMethod(String paymentMethod);

    List<Payment> findByPaymentType(String paymentType);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'Paid'")
    List<Payment> findSuccessfulPayments();

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'Pending'")
    List<Payment> findPendingPayments();
}