package com.ecodana.evodanavn1.model;

public class Vehicle {
    private String id;
    private String name;
    private String type;
    private String brand;
    private double price;
    private boolean requiresLicense;
    private String icon;
    private String gradient;

    // Constructors
    public Vehicle() {}
    public Vehicle(String id, String name, String type, String brand, double price, boolean requiresLicense, String icon, String gradient) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.price = price;
        this.requiresLicense = requiresLicense;
        this.icon = icon;
        this.gradient = gradient;
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isRequiresLicense() { return requiresLicense; }
    public void setRequiresLicense(boolean requiresLicense) { this.requiresLicense = requiresLicense; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getGradient() { return gradient; }
    public void setGradient(String gradient) { this.gradient = gradient; }
}