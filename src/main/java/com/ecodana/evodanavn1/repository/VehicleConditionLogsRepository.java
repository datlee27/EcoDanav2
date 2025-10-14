package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.VehicleConditionLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleConditionLogsRepository extends JpaRepository<VehicleConditionLogs, String> {
    
    /**
     * Find logs by booking ID
     */
    List<VehicleConditionLogs> findByBooking_BookingId(String bookingId);
    
    /**
     * Find logs by vehicle ID
     */
    List<VehicleConditionLogs> findByVehicle_VehicleId(String vehicleId);
    
    /**
     * Find logs by staff ID
     */
    List<VehicleConditionLogs> findByStaff_Id(String staffId);
    
    /**
     * Find logs by check type
     */
    List<VehicleConditionLogs> findByCheckType(String checkType);
    
    /**
     * Find pickup log for a booking
     */
    Optional<VehicleConditionLogs> findByBooking_BookingIdAndCheckType(String bookingId, String checkType);
    
    /**
     * Find logs by staff and date range
     */
    @Query("SELECT vcl FROM VehicleConditionLogs vcl WHERE vcl.staff.id = :staffId AND vcl.checkTime BETWEEN :startDate AND :endDate ORDER BY vcl.checkTime DESC")
    List<VehicleConditionLogs> findByStaffAndDateRange(@Param("staffId") String staffId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count logs by staff
     */
    long countByStaff_Id(String staffId);
    
    /**
     * Find recent logs
     */
    @Query("SELECT vcl FROM VehicleConditionLogs vcl ORDER BY vcl.checkTime DESC")
    List<VehicleConditionLogs> findRecentLogs();
}
