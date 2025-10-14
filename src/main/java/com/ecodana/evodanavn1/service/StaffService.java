package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.*;
import com.ecodana.evodanavn1.repository.BookingApprovalRepository;
import com.ecodana.evodanavn1.repository.BookingRepository;
import com.ecodana.evodanavn1.repository.VehicleConditionLogsRepository;
import com.ecodana.evodanavn1.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StaffService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingApprovalRepository bookingApprovalRepository;

    @Autowired
    private VehicleConditionLogsRepository vehicleConditionLogsRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all pending bookings that need staff approval
     */
    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(Booking.BookingStatus.Pending);
    }

    /**
     * Get approved bookings ready for pickup
     */
    public List<Booking> getApprovedBookings() {
        return bookingRepository.findByStatus(Booking.BookingStatus.Approved);
    }

    /**
     * Get ongoing bookings (vehicles currently rented)
     */
    public List<Booking> getOngoingBookings() {
        return bookingRepository.findByStatus(Booking.BookingStatus.Ongoing);
    }

    /**
     * Approve a booking
     */
    @Transactional
    public BookingApproval approveBooking(String bookingId, User staff, String note) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        
        // Check if already approved/rejected
        if (booking.getStatus() != Booking.BookingStatus.Pending) {
            throw new RuntimeException("Booking is not in pending status");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.Approved);
        booking.setHandledBy(staff);
        bookingRepository.save(booking);

        // Create approval record
        BookingApproval approval = new BookingApproval();
        approval.setApprovalId(UUID.randomUUID().toString());
        approval.setBooking(booking);
        approval.setStaff(staff);
        approval.setApprovalStatus("Approved");
        approval.setApprovalDate(LocalDateTime.now());
        approval.setNote(note);
        bookingApprovalRepository.save(approval);

        // Send notification to customer
        try {
            String message = String.format(
                "✅ Đơn đặt xe #%s đã được duyệt! Vui lòng đến nhận xe vào %s",
                booking.getBookingCode(),
                booking.getPickupDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            notificationService.createNotification(
                booking.getUser().getId(),
                message,
                booking.getBookingId(),
                "BOOKING_APPROVED"
            );
        } catch (Exception e) {
            System.out.println("Warning: Failed to send notification: " + e.getMessage());
        }

        return approval;
    }

    /**
     * Reject a booking
     */
    @Transactional
    public BookingApproval rejectBooking(String bookingId, User staff, String rejectionReason) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        
        // Check if already approved/rejected
        if (booking.getStatus() != Booking.BookingStatus.Pending) {
            throw new RuntimeException("Booking is not in pending status");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.Rejected);
        booking.setHandledBy(staff);
        booking.setCancelReason(rejectionReason);
        bookingRepository.save(booking);

        // Create approval record
        BookingApproval approval = new BookingApproval();
        approval.setApprovalId(UUID.randomUUID().toString());
        approval.setBooking(booking);
        approval.setStaff(staff);
        approval.setApprovalStatus("Rejected");
        approval.setApprovalDate(LocalDateTime.now());
        approval.setRejectionReason(rejectionReason);
        bookingApprovalRepository.save(approval);

        // Send notification to customer
        try {
            String message = String.format(
                "❌ Đơn đặt xe #%s đã bị từ chối. Lý do: %s",
                booking.getBookingCode(),
                rejectionReason
            );
            notificationService.createNotification(
                booking.getUser().getId(),
                message,
                booking.getBookingId(),
                "BOOKING_REJECTED"
            );
        } catch (Exception e) {
            System.out.println("Warning: Failed to send notification: " + e.getMessage());
        }

        return approval;
    }

    /**
     * Record vehicle pickup condition
     */
    @Transactional
    public VehicleConditionLogs recordPickupCondition(
            String bookingId, 
            User staff, 
            Integer odometer,
            String batteryLevel,
            String conditionStatus,
            String conditionDescription,
            String damageImages,
            String note) {
        
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        Vehicle vehicle = booking.getVehicle();

        // Check if pickup already recorded
        Optional<VehicleConditionLogs> existingLog = 
            vehicleConditionLogsRepository.findByBooking_BookingIdAndCheckType(bookingId, "Pickup");
        if (existingLog.isPresent()) {
            throw new RuntimeException("Pickup condition already recorded");
        }

        // Create condition log
        VehicleConditionLogs log = new VehicleConditionLogs();
        log.setLogId(UUID.randomUUID().toString());
        log.setBooking(booking);
        log.setVehicle(vehicle);
        log.setStaff(staff);
        log.setCheckType("Pickup");
        log.setCheckTime(LocalDateTime.now());
        log.setOdometer(odometer);
        log.setFuelLevel(batteryLevel);
        log.setConditionStatus(conditionStatus);
        log.setConditionDescription(conditionDescription);
        log.setDamageImages(damageImages);
        log.setNote(note);
        vehicleConditionLogsRepository.save(log);

        // Update booking status to Ongoing
        booking.setStatus(Booking.BookingStatus.Ongoing);
        bookingRepository.save(booking);

        // Update vehicle status
        vehicle.setStatus(Vehicle.VehicleStatus.Rented);
        vehicle.setOdometer(odometer);
        vehicleRepository.save(vehicle);

        // Send notification to customer
        try {
            String message = String.format(
                "🚗 Xe %s đã được giao! Chúc bạn có chuyến đi an toàn và vui vẻ.",
                vehicle.getVehicleModel()
            );
            notificationService.createNotification(
                booking.getUser().getId(),
                message,
                booking.getBookingId(),
                "VEHICLE_PICKUP"
            );
        } catch (Exception e) {
            System.out.println("Warning: Failed to send notification: " + e.getMessage());
        }

        return log;
    }

    /**
     * Record vehicle return condition
     */
    @Transactional
    public VehicleConditionLogs recordReturnCondition(
            String bookingId, 
            User staff, 
            Integer odometer,
            String batteryLevel,
            String conditionStatus,
            String conditionDescription,
            String damageImages,
            String note) {
        
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        Vehicle vehicle = booking.getVehicle();

        // Check if return already recorded
        Optional<VehicleConditionLogs> existingLog = 
            vehicleConditionLogsRepository.findByBooking_BookingIdAndCheckType(bookingId, "Return");
        if (existingLog.isPresent()) {
            throw new RuntimeException("Return condition already recorded");
        }

        // Create condition log
        VehicleConditionLogs log = new VehicleConditionLogs();
        log.setLogId(UUID.randomUUID().toString());
        log.setBooking(booking);
        log.setVehicle(vehicle);
        log.setStaff(staff);
        log.setCheckType("Return");
        log.setCheckTime(LocalDateTime.now());
        log.setOdometer(odometer);
        log.setFuelLevel(batteryLevel);
        log.setConditionStatus(conditionStatus);
        log.setConditionDescription(conditionDescription);
        log.setDamageImages(damageImages);
        log.setNote(note);
        vehicleConditionLogsRepository.save(log);

        // Update booking status to Completed
        booking.setStatus(Booking.BookingStatus.Completed);
        bookingRepository.save(booking);

        // Update vehicle status and odometer
        vehicle.setStatus(Vehicle.VehicleStatus.Available);
        vehicle.setOdometer(odometer);
        vehicleRepository.save(vehicle);

        // Send notification to customer
        try {
            String message = String.format(
                "✅ Xe %s đã được trả thành công! Cảm ơn bạn đã sử dụng dịch vụ EcoDana.",
                vehicle.getVehicleModel()
            );
            notificationService.createNotification(
                booking.getUser().getId(),
                message,
                booking.getBookingId(),
                "VEHICLE_RETURN"
            );
        } catch (Exception e) {
            System.out.println("Warning: Failed to send notification: " + e.getMessage());
        }

        return log;
    }

    /**
     * Get booking details with condition logs
     */
    public Map<String, Object> getBookingDetails(String bookingId) {
        Map<String, Object> details = new HashMap<>();
        
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return details;
        }
        
        Booking booking = bookingOpt.get();
        details.put("booking", booking);
        
        // Get approval record
        Optional<BookingApproval> approval = bookingApprovalRepository.findByBooking_BookingId(bookingId);
        approval.ifPresent(value -> details.put("approval", value));
        
        // Get condition logs
        List<VehicleConditionLogs> logs = vehicleConditionLogsRepository.findByBooking_BookingId(bookingId);
        details.put("conditionLogs", logs);
        
        return details;
    }

    /**
     * Get staff statistics
     */
    public Map<String, Object> getStaffStatistics(String staffId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Count approvals
        long totalApprovals = bookingApprovalRepository.countByStaff_Id(staffId);
        long approvedCount = bookingApprovalRepository.countByStaff_IdAndApprovalStatus(staffId, "Approved");
        long rejectedCount = bookingApprovalRepository.countByStaff_IdAndApprovalStatus(staffId, "Rejected");
        
        stats.put("totalApprovals", totalApprovals);
        stats.put("approvedCount", approvedCount);
        stats.put("rejectedCount", rejectedCount);
        
        // Count vehicle handovers
        long totalHandovers = vehicleConditionLogsRepository.countByStaff_Id(staffId);
        stats.put("totalHandovers", totalHandovers);
        
        return stats;
    }

    /**
     * Get vehicle condition logs for a booking
     */
    public List<VehicleConditionLogs> getVehicleConditionLogs(String bookingId) {
        return vehicleConditionLogsRepository.findByBooking_BookingId(bookingId);
    }

    /**
     * Get pickup log for a booking
     */
    public Optional<VehicleConditionLogs> getPickupLog(String bookingId) {
        return vehicleConditionLogsRepository.findByBooking_BookingIdAndCheckType(bookingId, "Pickup");
    }

    /**
     * Get return log for a booking
     */
    public Optional<VehicleConditionLogs> getReturnLog(String bookingId) {
        return vehicleConditionLogsRepository.findByBooking_BookingIdAndCheckType(bookingId, "Return");
    }
}
