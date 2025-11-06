package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalTime;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import com.ecodana.evodanavn1.model.VehicleConditionLogs;
import com.ecodana.evodanavn1.repository.VehicleConditionLogsRepository;
import com.ecodana.evodanavn1.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

//    @Value("${booking.owner-approval-timeout-hours}")
//    private int ownerApprovalTimeoutHours;

    @Value("${booking.owner-approval-timeout-minutes}")
    private int ownerApprovalTimeoutMinutes;

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

    public Booking getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
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

    /**
     * Tác vụ định kỳ: Tự động từ chối các booking 'Pending' đã quá hạn.
     * Chạy mỗi 15 phút.
     */
    @Scheduled(fixedRate = 15000) // 15000ms = 15 giây
    @Transactional
    public void autoRejectExpiredBookings() {
        logger.info("Chạy tác vụ tự động hủy booking quá hạn (Test)...");

        // 1. Xác định mốc thời gian timeout (theo phút)
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(ownerApprovalTimeoutMinutes);

        // 2. Tìm tất cả booking 'Pending' đã quá hạn
        List<Booking> expiredBookings = bookingRepository.findPendingBookingsOlderThan(timeoutThreshold);

        if (expiredBookings.isEmpty()) {
            logger.info("Không có booking nào quá hạn (Timeout = {} phút).", ownerApprovalTimeoutMinutes);
            return;
        }

        logger.warn("Tìm thấy {} booking quá hạn. Bắt đầu xử lý...", expiredBookings.size());

        // 3. Xử lý từng booking
        for (Booking booking : expiredBookings) {
            try {
                // Lấy thông tin xe trước khi thay đổi
                Vehicle vehicle = booking.getVehicle();
                String customerId = booking.getUser().getId();
                String bookingCode = booking.getBookingCode();

                // 4. Cập nhật trạng thái Booking
                booking.setStatus(Booking.BookingStatus.Rejected);
                booking.setCancelReason("Chủ xe không phản hồi yêu cầu (tự động hủy).");
                bookingRepository.save(booking);

                // 5. Mở lại xe cho người khác đặt
                // (Logic này đã bao gồm việc kiểm tra các booking khác của xe)
                updateVehicleStatusOnBookingCompletionOrCancellation(vehicle);

                // 6. Gửi thông báo cho khách hàng
                // (Phương thức này đã tồn tại trong NotificationService)
                notificationService.notifyBookingAutoRejected(booking);

                logger.info("Đã tự động hủy booking: {} (Khách: {}, Xe: {})", bookingCode, customerId, vehicle.getLicensePlate());

            } catch (Exception e) {
                logger.error("Lỗi khi tự động hủy booking {}: {}", booking.getBookingId(), e.getMessage(), e);
            }
        }
        logger.info("Hoàn tất tác vụ tự động hủy booking.");
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

    /**
     * Lấy phân tích doanh thu chi tiết cho một chủ xe cụ thể.
     * Chỉ tính các booking đã 'Completed' (Hoàn thành) hoặc 'Confirmed' (Đã thanh toán cọc).
     * @param ownerId ID của chủ xe
     * @return Map chứa revenueToday, revenueThisMonth, revenueThisYear, totalRevenueAllTime
     */
    public Map<String, Object> getOwnerRevenueAnalytics(String ownerId) {
        Map<String, Object> analytics = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Xác định các mốc thời gian
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();

        // 1. Lấy TẤT CẢ booking của owner đó
        List<Booking> ownerBookings = bookingRepository.findByVehicleOwnerId(ownerId);

        // 2. Lọc các booking đã mang lại doanh thu (Hoàn thành hoặc Đã xác nhận/đã cọc)
        List<Booking> revenueBookings = ownerBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.Completed || b.getStatus() == Booking.BookingStatus.Confirmed)
                .collect(Collectors.toList());

        // 3. Tính toán doanh thu dựa trên *ngày booking được tạo*
        // (Lưu ý: Bạn có thể thay đổi logic này để dựa trên ngày hoàn thành (CompletedDate) nếu cần)

        BigDecimal revenueToday = revenueBookings.stream()
                .filter(b -> b.getCreatedDate().isAfter(startOfToday))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenueThisMonth = revenueBookings.stream()
                .filter(b -> b.getCreatedDate().isAfter(startOfMonth))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenueThisYear = revenueBookings.stream()
                .filter(b -> b.getCreatedDate().isAfter(startOfYear))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenueAllTime = revenueBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        analytics.put("revenueToday", revenueToday != null ? revenueToday : BigDecimal.ZERO);
        analytics.put("revenueThisMonth", revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO);
        analytics.put("revenueThisYear", revenueThisYear != null ? revenueThisYear : BigDecimal.ZERO);
        analytics.put("totalRevenueAllTime", totalRevenueAllTime != null ? totalRevenueAllTime : BigDecimal.ZERO);

        return analytics;
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu cho owner (Ngày, Tháng, Năm)
     * @param ownerId ID của chủ xe
     * @return Map chứa dữ liệu cho 3 biểu đồ
     */
    public Map<String, Object> getOwnerRevenueChartData(String ownerId) {
        Map<String, Object> chartData = new HashMap<>();

        // 1. Dữ liệu 7 ngày qua (Daily)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);
        List<Map<String, Object>> dailyResults = bookingRepository.findDailyRevenueForOwner(ownerId, sevenDaysAgo);
        chartData.put("dailyLabels", dailyResults.stream().map(r -> r.get("period").toString()).collect(Collectors.toList()));
        chartData.put("dailyData", dailyResults.stream().map(r -> (BigDecimal) r.get("revenue")).collect(Collectors.toList()));

        // 2. Dữ liệu 12 tháng qua (Monthly)
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12).withDayOfMonth(1).with(LocalTime.MIN);
        List<Map<String, Object>> monthlyResults = bookingRepository.findMonthlyRevenueForOwner(ownerId, twelveMonthsAgo);
        chartData.put("monthlyLabels", monthlyResults.stream().map(r -> r.get("period").toString()).collect(Collectors.toList()));
        chartData.put("monthlyData", monthlyResults.stream().map(r -> (BigDecimal) r.get("revenue")).collect(Collectors.toList()));

        // 3. Dữ liệu 5 năm qua (Yearly)
        LocalDateTime fiveYearsAgo = LocalDateTime.now().minusYears(5).withDayOfYear(1).with(LocalTime.MIN);
        List<Map<String, Object>> yearlyResults = bookingRepository.findYearlyRevenueForOwner(ownerId, fiveYearsAgo);
        chartData.put("yearlyLabels", yearlyResults.stream().map(r -> r.get("period").toString()).collect(Collectors.toList()));
        chartData.put("yearlyData", yearlyResults.stream().map(r -> (BigDecimal) r.get("revenue")).collect(Collectors.toList()));

        return chartData;
    }
}