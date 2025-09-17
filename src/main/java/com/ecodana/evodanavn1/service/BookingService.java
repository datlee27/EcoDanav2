package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    private List<Booking> bookings = new ArrayList<>();
    public List<Booking> getAllBookings() {
        return bookings;
    }

    public BookingService() {
        // Mock data cho dashboard
        bookings.add(new Booking("1", new User("1", "demo", "demo@example.com", "customer", true),
                new Vehicle("tesla-model-3", "Tesla Model 3", "Electric Car", "Tesla", 89, true, "fas fa-car", "from-primary to-secondary"),
                LocalDate.of(2024, 12, 15), LocalDate.of(2024, 12, 18), 267, "Confirmed"));
        bookings.add(new Booking("2", new User("1", "demo", "demo@example.com", "customer", true),
                new Vehicle("vinfast-klara", "VinFast Klara", "Electric Motorcycle", "VinFast", 25, false, "fas fa-motorcycle", "from-secondary to-primary"),
                LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 12), 50, "Completed"));
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookings.stream().filter(b -> b.getUser().getId().equals(user.getId())).toList();
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }
}