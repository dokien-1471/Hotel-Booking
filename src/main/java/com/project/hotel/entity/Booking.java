package com.project.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
=======
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
>>>>>>> 8eba4e09bc0792bc00a45d21d5208f350a00e30e
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
<<<<<<< HEAD

=======
    
    @Column(unique = true)
>>>>>>> 8eba4e09bc0792bc00a45d21d5208f350a00e30e
    private String bookingReference;

    @Column(nullable = false)
    private LocalDate bookingDate;
<<<<<<< HEAD
}
=======
    
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}
>>>>>>> 8eba4e09bc0792bc00a45d21d5208f350a00e30e
