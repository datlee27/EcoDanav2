package com.ecodana.evodanavn1.repository;

import java.util.List;
import java.util.Map;
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
    
    /**
     * Find bookings by status and date range
     * @param status the booking status
     * @param startDate start date
     * @param endDate end date
     * @return list of bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.pickupDateTime >= :startDate AND b.returnDateTime <= :endDate")
    List<Booking> findByStatusAndDateRange(@Param("status") String status, 
                                         @Param("startDate") java.time.LocalDateTime startDate, 
                                         @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Find recent bookings
     * @return list of recent bookings
     */
    @Query("SELECT b FROM Booking b ORDER BY b.createdDate DESC")
    List<Booking> findRecentBookings();
    
    /**
     * Find daily bookings for the last N days
     * @param startDate start date
     * @return list of daily booking counts
     */
    @Query(value = "SELECT DATE(b.CreatedDate) as date, COUNT(b.BookingId) as count FROM Booking b " +
           "WHERE b.CreatedDate >= :startDate " +
           "GROUP BY DATE(b.CreatedDate) ORDER BY date DESC", nativeQuery = true)
    List<Map<String, Object>> findDailyBookings(@Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Find monthly revenue for the last N months
     * @param months number of months
     * @return list of monthly revenue
     */
    @Query(value = "SELECT YEAR(b.CreatedDate) as year, MONTH(b.CreatedDate) as month, SUM(b.TotalAmount) as revenue " +
           "FROM Booking b WHERE b.Status IN ('Confirmed', 'Completed') " +
           "AND b.CreatedDate >= :startDate " +
           "GROUP BY YEAR(b.CreatedDate), MONTH(b.CreatedDate) ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Map<String, Object>> findMonthlyRevenue(@Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Find vehicle popularity (most booked vehicles)
     * @return list of vehicle popularity data
     */
    @Query(value = "SELECT cb.BrandName as brand, v.VehicleModel as model, COUNT(b.BookingId) as bookingCount " +
           "FROM Booking b " +
           "JOIN Vehicle v ON b.VehicleId = v.VehicleId " +
           "JOIN CarBrand cb ON v.BrandId = cb.BrandId " +
           "WHERE b.Status IN ('Confirmed', 'Completed') " +
           "GROUP BY cb.BrandName, v.VehicleModel ORDER BY bookingCount DESC", nativeQuery = true)
    List<Map<String, Object>> findVehiclePopularity();
}
