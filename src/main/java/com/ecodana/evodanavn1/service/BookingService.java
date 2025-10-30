package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.model.VehicleConditionLogs;
import com.ecodana.evodanavn1.repository.VehicleConditionLogsRepository;
import com.ecodana.evodanavn1.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.repository.BookingRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleConditionLogsRepository vehicleConditionLogsRepository;

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUserId(user.getId());
    }
    public List<Booking> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public void addBooking(Booking booking) {
        if (booking.getBookingId() == null) {
            booking.setBookingId(UUID.randomUUID().toString());
        }
        if (booking.getBookingCode() == null) {
            booking.setBookingCode("BK" + System.currentTimeMillis());
        }
        bookingRepository.save(booking);
    }

    public List<Booking> getActiveBookingsByUser(User user) {
        return bookingRepository.findActiveBookingsByUserId(user.getId());
    }

    public List<Booking> getActiveBookings() {
        return bookingRepository.findAllActiveBookings();
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findAllPendingBookings();
    }

    public BigDecimal getTodayRevenue() {
        List<Booking> completedBookings = bookingRepository.findByStatus(Booking.BookingStatus.Completed);
        return completedBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalRevenue() {
        List<Booking> revenueBookings = bookingRepository.findAll();
        return revenueBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Approved || b.getStatus() == Booking.BookingStatus.Completed)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Object> getReviewsByUser(User user) {
        return List.of();
    }

    public java.util.Optional<Booking> findById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Booking updateBookingDetails(String bookingId, Map<String, String> data) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    if (data.containsKey("pickupDateTime")) {
                        booking.setPickupDateTime(LocalDateTime.parse(data.get("pickupDateTime")));
                    }
                    if (data.containsKey("returnDateTime")) {
                        booking.setReturnDateTime(LocalDateTime.parse(data.get("returnDateTime")));
                    }
                    if (data.containsKey("totalAmount")) {
                        booking.setTotalAmount(new BigDecimal(data.get("totalAmount")));
                    }
                    if (data.containsKey("status")) {
                        booking.setStatus(Booking.BookingStatus.valueOf(data.get("status")));
                    }
                    return bookingRepository.save(booking);
                })
                .orElse(null);
    }

    public void deleteBooking(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    public Map<String, Object> getRevenueAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("todayRevenue", getTodayRevenue());
        analytics.put("monthRevenue", getThisMonthRevenue());
        analytics.put("totalRevenue", getTotalRevenue());
        analytics.put("revenueGrowth", 15.5); // Mock data
        return analytics;
    }

    public BigDecimal getThisMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        List<Booking> monthBookings = bookingRepository.findByStatusAndDateRange(Booking.BookingStatus.Completed, startOfMonth, endOfMonth);
        return monthBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Booking> allBookings = getAllBookings();
        stats.put("totalBookings", allBookings.size());
        stats.put("pendingBookings", getPendingBookings().size());
        stats.put("activeBookings", getActiveBookings().size());
        stats.put("cancelledBookings", allBookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.Cancelled).count());
        return stats;
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingRepository.findRecentBookings().stream().limit(limit).collect(Collectors.toList());
    }

    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public Booking updateBookingStatus(String bookingId, String status) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.valueOf(status));
                    return bookingRepository.save(booking);
                })
                .orElse(null);
    }

    public Map<String, Object> getBookingAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            analytics.put("dailyBookings", bookingRepository.findDailyBookings(sevenDaysAgo));
        } catch (Exception e) {
            analytics.put("dailyBookings", List.of());
        }
        try {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            analytics.put("monthlyRevenue", bookingRepository.findMonthlyRevenue(sixMonthsAgo));
        } catch (Exception e) {
            analytics.put("monthlyRevenue", List.of());
        }
        try {
            analytics.put("vehiclePopularity", bookingRepository.findVehiclePopularity());
        } catch (Exception e) {
            analytics.put("vehiclePopularity", List.of());
        }
        return analytics;
    }

    // Owner Management Methods
    public List<Booking> getBookingsByOwnerId(String ownerId) {
        return bookingRepository.findByVehicleOwnerId(ownerId);
    }

    public Booking approveBooking(String bookingId, User approver) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    // Chuyển sang AwaitingDeposit - yêu cầu customer thanh toán
                    booking.setStatus(Booking.BookingStatus.AwaitingDeposit);
                    booking.setHandledBy(approver);
                    // Không set vehicle status thành Rented ngay - chỉ set khi đã thanh toán
                    return bookingRepository.save(booking);
                })
                .orElse(null);
    }

    public Booking rejectBooking(String bookingId, String reason, User rejector) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.Rejected);
                    booking.setCancelReason(reason);
                    booking.setHandledBy(rejector);
                    Booking updatedBooking = bookingRepository.save(booking);

                    // Update vehicle status back to Available if no other active bookings exist
                    updateVehicleStatusOnBookingCompletionOrCancellation(booking.getVehicle());

                    return updatedBooking;
                })
                .orElse(null);
    }

    public Booking completeBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.Completed);
                    // Update vehicle status back to Available when booking is completed
                    Booking updatedBooking = bookingRepository.save(booking);
                    updateVehicleStatusOnBookingCompletionOrCancellation(booking.getVehicle());
                    return updatedBooking;
                })
                .orElse(null);
    }

    public Booking cancelBooking(String bookingId, String reason) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(Booking.BookingStatus.Cancelled);
                    booking.setCancelReason(reason);
                    // Update vehicle status back to Available when booking is cancelled
                    Booking updatedBooking = bookingRepository.save(booking);
                    updateVehicleStatusOnBookingCompletionOrCancellation(booking.getVehicle());
                    return updatedBooking;
                })
                .orElse(null);
    }

    /**
     * Updates the vehicle status to 'Available' if there are no other active
     * (Approved, Ongoing) bookings for it.
     * @param vehicle The vehicle to check and update.
     */
    private void updateVehicleStatusOnBookingCompletionOrCancellation(Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }
        // Check if there are any other 'Approved' or 'Ongoing' bookings for this vehicle
        boolean hasOtherActiveBookings = bookingRepository.hasActiveBookings(vehicle.getVehicleId());

        if (!hasOtherActiveBookings) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            vehicleService.updateVehicle(vehicle);
        }
    }

    public Map<String, Long> getBookingCountsByStatus() {
        Map<String, Long> counts = new HashMap<>();
        List<Booking> allBookings = getAllBookings();
        
        counts.put("pending", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Pending).count());
        counts.put("approved", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Approved).count());
        counts.put("ongoing", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Ongoing).count());
        counts.put("completed", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Completed).count());
        counts.put("rejected", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Rejected).count());
        counts.put("cancelled", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Cancelled).count());
        
        return counts;
    }

    public Booking cancelCar(String bookingId, String reason) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    // Set status back to Cancelled
                    booking.setStatus(Booking.BookingStatus.Cancelled);
                    booking.setCancelReason(reason);

                    // Update vehicle status back to Available when car is cancelled after payment
                    Booking updatedBooking = bookingRepository.save(booking);
                    updateVehicleStatusOnBookingCompletionOrCancellation(booking.getVehicle());

                    return updatedBooking;
                })
                .orElse(null);
    }


    /**
     * Xử lý nghiệp vụ Giao xe (Handover)
     * @param bookingId ID của booking
     * @param owner Người dùng (owner) thực hiện giao xe
     * @param imageUrls Danh sách URL ảnh đã tải lên Cloudinary
     * @param odometer Số Odometer lúc giao
     * @param notes Ghi chú lúc giao
     * @return Booking đã cập nhật
     * @throws Exception
     */
    @Transactional
    public Booking handoverVehicle(String bookingId, User owner, List<String> imageUrls, int odometer, String notes) throws Exception {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking với ID: " + bookingId));

        // 1. Chỉ cho phép giao xe khi status là Confirmed
        if (booking.getStatus() != Booking.BookingStatus.Confirmed) {
            throw new IllegalStateException("Không thể giao xe. Trạng thái booking không phải là 'Confirmed'.");
        }

        Vehicle vehicle = booking.getVehicle();
        if (vehicle == null) {
            throw new RuntimeException("Không tìm thấy Vehicle cho booking này.");
        }

        // 2. Cập nhật trạng thái Booking và Vehicle
        booking.setStatus(Booking.BookingStatus.Ongoing);
        vehicle.setStatus(Vehicle.VehicleStatus.Rented);

        // 3. Tạo Log ghi nhận tình trạng xe lúc giao (Pickup)
        VehicleConditionLogs log = new VehicleConditionLogs();
        log.setLogId(UUID.randomUUID().toString());
        log.setBooking(booking);
        log.setVehicle(vehicle);
        log.setStaff(owner); // Ghi nhận owner là người giao xe
        log.setCheckType("Pickup"); // Đánh dấu đây là log lúc Giao xe
        log.setCheckTime(LocalDateTime.now());
        log.setOdometer(odometer);
        log.setNote(notes);

        // Lưu ảnh dưới dạng JSON
        if (imageUrls != null && !imageUrls.isEmpty()) {
            log.setDamageImages(objectMapper.writeValueAsString(imageUrls));
        }

        // 4. Lưu tất cả thay đổi
        vehicleConditionLogsRepository.save(log);
        vehicleRepository.save(vehicle);
        Booking updatedBooking = bookingRepository.save(booking);

        // 5. Gửi thông báo cho khách hàng
        notificationService.notifyCustomerRentalStarted(updatedBooking);

        return updatedBooking;
    }
}