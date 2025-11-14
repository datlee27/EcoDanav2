package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId")
    List<Payment> findByBookingId(@Param("bookingId") String bookingId);
    Optional<Payment> findByOrderCode(String orderCode);
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN p.booking b JOIN b.vehicle v WHERE v.ownerId = :ownerId")
    List<Payment> findPaymentsByVehicleOwnerId(@Param("ownerId") String ownerId);
}
