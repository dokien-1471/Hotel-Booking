package com.project.hotel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @Column(nullable = false)
    private String method; // VNPAY, MOMO, COD, etc.

    @Column(nullable = false)
    private String status; // PENDING, SUCCESS, FAILED

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(unique = true)
    private String transactionId;
    
    @Column(nullable = false, unique = true)
    private String paymentReference;
}