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
}