package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecodana.evodanavn1.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    
    /**
     * Find bookings by user ID
     * @param userId the user ID
     * @return list of bookings for the user
     */
    List<Booking> findByUserId(String userId);
    
    /**
     * Find bookings by vehicle ID
     * @param vehicleId the vehicle ID
     * @return list of bookings for the vehicle
     */
    List<Booking> findByVehicleId(String vehicleId);
    
    /**
     * Find bookings by status
     * @param status the booking status
     * @return list of bookings with the status
     */
    List<Booking> findByStatus(String status);
    
    /**
     * Find active bookings for a user (Confirmed or Pending)
     * @param userId the user ID
     * @return list of active bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status IN ('Confirmed', 'Pending')")
    List<Booking> findActiveBookingsByUserId(@Param("userId") String userId);
    
    /**
     * Find all active bookings
     * @return list of all active bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'Confirmed'")
    List<Booking> findAllActiveBookings();
    
    /**
     * Find all pending bookings
     * @return list of all pending bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'Pending'")
    List<Booking> findAllPendingBookings();
    
    /**
     * Find booking by booking code
     * @param bookingCode the booking code
     * @return optional booking
     */
    Optional<Booking> findByBookingCode(String bookingCode);
    
    /**
     * Check if booking exists by booking code
     * @param bookingCode the booking code
     * @return true if exists, false otherwise
     */
    boolean existsByBookingCode(String bookingCode);
}
