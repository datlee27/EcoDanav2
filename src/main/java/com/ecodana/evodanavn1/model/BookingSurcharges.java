package com.ecodana.evodanavn1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "BookingSurcharges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSurcharges {
    @Id
    @Column(name = "SurchargeId", length = 36)
    private String surchargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BookingId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Booking booking;

    @Column(name = "SurchargeType", length = 50, nullable = false)
    private String surchargeType;

    @Column(name = "Amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "SurchargeCategory", length = 50)
    private String surchargeCategory;

    @Column(name = "IsSystemGenerated", nullable = false)
    private boolean isSystemGenerated;
}