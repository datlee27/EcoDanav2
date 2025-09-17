package com.ecodana.evodanavn1.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String role;  // customer, staff, admin
    private boolean hasLicense;

    // Constructors
    public User() {}
    public User(String id, String username, String email, String role, boolean hasLicense) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.hasLicense = hasLicense;
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isHasLicense() { return hasLicense; }
    public void setHasLicense(boolean hasLicense) { this.hasLicense = hasLicense; }
}