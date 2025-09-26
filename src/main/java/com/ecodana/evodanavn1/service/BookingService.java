package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
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
}