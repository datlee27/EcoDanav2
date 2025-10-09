package com.ecodana.evodanavn1.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "UserFavoriteVehicles")
@IdClass(UserFavoriteVehicles.UserFavoriteVehicleId.class)
public class UserFavoriteVehicles {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user; // Sửa từ 'Users' thành 'user' và kiểu dữ liệu

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VehicleId", nullable = false)
    private Vehicle vehicle;

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; } // Sửa kiểu dữ liệu
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    // Composite key class
    public static class UserFavoriteVehicleId implements Serializable {
        private String user; // Tên này phải khớp với thuộc tính 'user' trong Entity
        private String vehicle;

        public UserFavoriteVehicleId() {}

        public UserFavoriteVehicleId(String user, String vehicle) {
            this.user = user;
            this.vehicle = vehicle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserFavoriteVehicleId that = (UserFavoriteVehicleId) o;
            return Objects.equals(user, that.user) && Objects.equals(vehicle, that.vehicle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, vehicle);
        }
    }
}