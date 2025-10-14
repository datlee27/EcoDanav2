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

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") String userId);

    @Query("SELECT b FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId")
    List<Booking> findByVehicleId(@Param("vehicleId") String vehicleId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status IN (com.ecodana.evodanavn1.model.Booking$BookingStatus.Approved, com.ecodana.evodanavn1.model.Booking$BookingStatus.Pending)")
    List<Booking> findActiveBookingsByUserId(@Param("userId") String userId);

    @Query("SELECT b FROM Booking b WHERE b.status = com.ecodana.evodanavn1.model.Booking$BookingStatus.Approved")
    List<Booking> findAllActiveBookings();

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId AND b.status IN (com.ecodana.evodanavn1.model.Booking$BookingStatus.Approved, com.ecodana.evodanavn1.model.Booking$BookingStatus.Ongoing)")
    boolean hasActiveBookings(@Param("vehicleId") String vehicleId);

    @Query("SELECT b FROM Booking b WHERE b.status = com.ecodana.evodanavn1.model.Booking$BookingStatus.Pending")
    List<Booking> findAllPendingBookings();

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.pickupDateTime >= :startDate AND b.returnDateTime <= :endDate")
    List<Booking> findByStatusAndDateRange(@Param("status") Booking.BookingStatus status,
                                           @Param("startDate") java.time.LocalDateTime startDate,
                                           @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT b FROM Booking b ORDER BY b.createdDate DESC")
    List<Booking> findRecentBookings();

    @Query(value = "SELECT DATE(b.CreatedDate) as date, COUNT(b.BookingId) as count FROM Booking b " +
            "WHERE b.CreatedDate >= :startDate " +
            "GROUP BY DATE(b.CreatedDate) ORDER BY date DESC", nativeQuery = true)
    List<Map<String, Object>> findDailyBookings(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = "SELECT YEAR(b.CreatedDate) as year, MONTH(b.CreatedDate) as month, SUM(b.TotalAmount) as revenue " +
            "FROM Booking b WHERE b.Status IN ('Approved', 'Completed') " +
            "AND b.CreatedDate >= :startDate " +
            "GROUP BY YEAR(b.CreatedDate), MONTH(b.CreatedDate) ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Map<String, Object>> findMonthlyRevenue(@Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = "SELECT v.VehicleModel as model, COUNT(b.BookingId) as bookingCount " +
            "FROM Booking b " +
            "JOIN Vehicle v ON b.VehicleId = v.VehicleId " +
            "WHERE b.Status IN ('Approved', 'Completed') " +
            "GROUP BY v.VehicleModel ORDER BY bookingCount DESC", nativeQuery = true)
    List<Map<String, Object>> findVehiclePopularity();
}