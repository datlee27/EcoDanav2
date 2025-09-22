package com.ecodana.evodanavn1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecodana.evodanavn1.model.Booking;
import com.ecodana.evodanavn1.model.User;
import com.ecodana.evodanavn1.model.Vehicle;

@Service
public class BookingService {
    private List<Booking> bookings = new ArrayList<>();
    
    public List<Booking> getAllBookings() {
        return bookings;
    }

    public BookingService() {
        // Mock data cho dashboard
        User demoUser = new User();
        demoUser.setId("demo-user-id");
        demoUser.setUsername("demo");
        demoUser.setEmail("demo@example.com");
        demoUser.setPassword("password123");
        demoUser.setPhoneNumber("0123456789");
        // Role will be set via roleId, not the role field
        
        // Create mock vehicles
        Vehicle tesla = new Vehicle();
        tesla.setVehicleId("tesla-model-3");
        tesla.setVehicleModel("Tesla Model 3");
        tesla.setVehicleType("ElectricCar");
        tesla.setPricePerDay(new BigDecimal("89"));
        tesla.setStatus("Available");
        tesla.setSeats(5);
        tesla.setRequiresLicense(true);
        
        Vehicle vinfast = new Vehicle();
        vinfast.setVehicleId("vinfast-klara");
        vinfast.setVehicleModel("VinFast Klara");
        vinfast.setVehicleType("ElectricMotorcycle");
        vinfast.setPricePerDay(new BigDecimal("25"));
        vinfast.setStatus("Available");
        vinfast.setSeats(2);
        vinfast.setRequiresLicense(false);
        
        // Create mock bookings
        Booking booking1 = new Booking();
        booking1.setBookingId("1");
        booking1.setUserId(demoUser.getId());
        booking1.setVehicleId(tesla.getVehicleId());
        booking1.setPickupDateTime(LocalDate.of(2024, 12, 15).atStartOfDay());
        booking1.setReturnDateTime(LocalDate.of(2024, 12, 18).atStartOfDay());
        booking1.setTotalAmount(new BigDecimal("267"));
        booking1.setStatus("Confirmed");
        booking1.setBookingCode("BK001");
        booking1.setRentalType("daily");
        
        Booking booking2 = new Booking();
        booking2.setBookingId("2");
        booking2.setUserId(demoUser.getId());
        booking2.setVehicleId(vinfast.getVehicleId());
        booking2.setPickupDateTime(LocalDate.of(2024, 12, 10).atStartOfDay());
        booking2.setReturnDateTime(LocalDate.of(2024, 12, 12).atStartOfDay());
        booking2.setTotalAmount(new BigDecimal("50"));
        booking2.setStatus("Completed");
        booking2.setBookingCode("BK002");
        booking2.setRentalType("daily");
        
        bookings.add(booking1);
        bookings.add(booking2);
    }

    public List<Booking> getBookingsByUser(User user) {
        return bookings.stream().filter(b -> b.getUserId().equals(user.getId())).toList();
    }

    public void addBooking(Booking booking) {
        if (booking.getBookingId() == null) {
            booking.setBookingId(UUID.randomUUID().toString());
        }
        if (booking.getBookingCode() == null) {
            booking.setBookingCode("BK" + System.currentTimeMillis());
        }
        bookings.add(booking);
    }
}