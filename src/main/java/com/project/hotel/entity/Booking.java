package com.project.hotel.entity;

import com.project.hotel.constant.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    @Column(nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private LocalTime checkInTime = LocalTime.of(14, 0); // Default check-in time 2:00 PM

    @Column(nullable = false)
    private LocalTime checkOutTime = LocalTime.of(12, 0); // Default check-out time 12:00 PM

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    @Column(nullable = false)
    private BigDecimal totalPrice;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(unique = true)
    private String bookingReference;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookingDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String specialRequests;

    @Column
    private Integer numberOfGuests;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @Column
    private String cancellationReason;

    @Column
    private LocalDateTime cancellationDate;

    @PrePersist
    public void generateBookingReference() {
        if (bookingReference == null) {
            this.bookingReference = "BK" + System.currentTimeMillis();
        }
    }

    // Tính tổng tiền dựa trên giá phòng và số ngày
    public void calculateTotalPrice() {
        if (room != null && checkInDate != null && checkOutDate != null) {
            long numberOfDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            this.totalPrice = room.getPrice().multiply(BigDecimal.valueOf(numberOfDays));
        }
    }

    // Kiểm tra xem ngày check-out có sau ngày check-in không
    public boolean isValidDates() {
        return checkOutDate.isAfter(checkInDate);
    }

    // Kiểm tra xem phòng có trống trong khoảng thời gian này không
    public boolean isRoomAvailable() {
        return room != null && room.isAvailable();
    }
}
