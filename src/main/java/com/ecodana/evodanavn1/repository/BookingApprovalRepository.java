package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.BookingApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingApprovalRepository extends JpaRepository<BookingApproval, String> {
    
    /**
     * Find approval by booking ID
     */
    Optional<BookingApproval> findByBooking_BookingId(String bookingId);
    
    /**
     * Find all approvals by staff ID
     */
    List<BookingApproval> findByStaff_Id(String staffId);
    
    /**
     * Find approvals by status
     */
    List<BookingApproval> findByApprovalStatus(String approvalStatus);
    
    /**
     * Find approvals by staff and date range
     */
    @Query("SELECT ba FROM BookingApproval ba WHERE ba.staff.id = :staffId AND ba.approvalDate BETWEEN :startDate AND :endDate ORDER BY ba.approvalDate DESC")
    List<BookingApproval> findByStaffAndDateRange(@Param("staffId") String staffId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count approvals by staff
     */
    long countByStaff_Id(String staffId);
    
    /**
     * Count approvals by staff and status
     */
    long countByStaff_IdAndApprovalStatus(String staffId, String approvalStatus);
}
