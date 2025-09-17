package com.ecodana.evodanavn1.model;

import java.time.LocalDate;

public class Booking {
    private String id;
    private User user;
    private Vehicle vehicle;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private double amount;
    private String status;  // Pending, Confirmed, Completed

    // Constructors
    public Booking() {}
    public Booking(String id, User user, Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate, double amount, String status) {
        this.id = id;
        this.user = user;
        this.vehicle = vehicle;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.amount = amount;
        this.status = status;
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}