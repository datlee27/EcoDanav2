package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ecodana.evodanavn1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.repository.BookingRepository;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookingRepository.findByUserId(user.getId());
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
    
    /**
     * Get active bookings for a user
     * @param user the user
     * @return list of active bookings
     */
    public List<Booking> getActiveBookingsByUser(User user) {
        return bookingRepository.findActiveBookingsByUserId(user.getId());
    }
    
    /**
     * Get all active bookings
     * @return list of active bookings
     */
    public List<Booking> getActiveBookings() {
        return bookingRepository.findAllActiveBookings();
    }
    
    /**
     * Get all pending bookings
     * @return list of pending bookings
     */
    public List<Booking> getPendingBookings() {
        return bookingRepository.findAllPendingBookings();
    }
    
    /**
     * Get today's revenue
     * @return today's revenue
     */
    public BigDecimal getTodayRevenue() {
        List<Booking> confirmedBookings = bookingRepository.findByStatus("Confirmed");
        return confirmedBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total revenue
     * @return total revenue
     */
    public BigDecimal getTotalRevenue() {
        List<Booking> revenueBookings = bookingRepository.findAll();
        return revenueBookings.stream()
                .filter(b -> "Confirmed".equals(b.getStatus()) || "Completed".equals(b.getStatus()))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get reviews by user (mock implementation)
     * @param user the user
     * @return list of reviews
     */
    public List<Object> getReviewsByUser(User user) {
        // Mock implementation - return empty list
        return List.of();
    }
    
    /**
     * Find booking by ID
     * @param bookingId the booking ID
     * @return optional booking
     */
    public java.util.Optional<Booking> findById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }
    
    /**
     * Update booking
     * @param booking the booking to update
     * @return updated booking
     */
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    /**
     * Delete booking
     * @param bookingId the booking ID to delete
     */
    public void deleteBooking(String bookingId) {
        bookingRepository.deleteById(bookingId);
    }
    
    /**
     * Get revenue analytics for admin dashboard
     * @return map containing revenue data
     */
    public Map<String, Object> getRevenueAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Today's revenue
        BigDecimal todayRevenue = getTodayRevenue();
        analytics.put("todayRevenue", todayRevenue);
        
        // This month's revenue
        BigDecimal monthRevenue = getThisMonthRevenue();
        analytics.put("monthRevenue", monthRevenue);
        
        // Total revenue
        BigDecimal totalRevenue = getTotalRevenue();
        analytics.put("totalRevenue", totalRevenue);
        
        // Revenue growth (mock data for now)
        analytics.put("revenueGrowth", 15.5); // 15.5% growth
        
        return analytics;
    }
    
    /**
     * Get this month's revenue
     * @return this month's revenue
     */
    public BigDecimal getThisMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        
        List<Booking> monthBookings = bookingRepository.findByStatusAndDateRange("Confirmed", startOfMonth, endOfMonth);
        return monthBookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get booking statistics for admin dashboard
     * @return map containing booking statistics
     */
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Booking> allBookings = getAllBookings();
        List<Booking> pendingBookings = getPendingBookings();
        List<Booking> activeBookings = getActiveBookings();
        
        stats.put("totalBookings", allBookings.size());
        stats.put("pendingBookings", pendingBookings.size());
        stats.put("activeBookings", activeBookings.size());
        stats.put("cancelledBookings", allBookings.stream()
                .mapToInt(b -> "Cancelled".equals(b.getStatus()) ? 1 : 0)
                .sum());
        
        return stats;
    }
    
    /**
     * Get recent bookings for admin dashboard
     * @param limit number of recent bookings to return
     * @return list of recent bookings
     */
    public List<Booking> getRecentBookings(int limit) {
        return bookingRepository.findRecentBookings().stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * Get bookings by status
     * @param status the booking status
     * @return list of bookings with the status
     */
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }
    
    /**
     * Update booking status
     * @param bookingId the booking ID
     * @param status the new status
     * @return updated booking or null if not found
     */
    public Booking updateBookingStatus(String bookingId, String status) {
        return bookingRepository.findById(bookingId)
                .map(booking -> {
                    booking.setStatus(status);
                    return bookingRepository.save(booking);
                })
                .orElse(null);
    }
    
    /**
     * Get booking analytics for charts
     * @return map containing chart data
     */
    public Map<String, Object> getBookingAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            // Daily bookings for the last 7 days
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Map<String, Object>> dailyBookings = bookingRepository.findDailyBookings(sevenDaysAgo);
            analytics.put("dailyBookings", dailyBookings);
        } catch (Exception e) {
            System.out.println("Error getting daily bookings: " + e.getMessage());
            analytics.put("dailyBookings", List.of());
        }
        
        try {
            // Monthly revenue for the last 6 months
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            List<Map<String, Object>> monthlyRevenue = bookingRepository.findMonthlyRevenue(sixMonthsAgo);
            analytics.put("monthlyRevenue", monthlyRevenue);
        } catch (Exception e) {
            System.out.println("Error getting monthly revenue: " + e.getMessage());
            analytics.put("monthlyRevenue", List.of());
        }
        
        try {
            // Vehicle popularity
            List<Map<String, Object>> vehiclePopularity = bookingRepository.findVehiclePopularity();
            analytics.put("vehiclePopularity", vehiclePopularity);
        } catch (Exception e) {
            System.out.println("Error getting vehicle popularity: " + e.getMessage());
            analytics.put("vehiclePopularity", List.of());
        }
        
        return analytics;
    }
}