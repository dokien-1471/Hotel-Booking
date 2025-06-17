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

/**
 * Entity đại diện cho đơn đặt phòng
 */
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
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "booking_reference", unique = true, nullable = false)
    private String bookingReference;

    @Column(name = "guest_full_name", nullable = false)
    private String guestFullName;

    @Column(name = "guest_email", nullable = false)
    private String guestEmail;

    @Positive(message = "Number of adults must be positive")
    @Column(name = "num_of_adults", nullable = false)
    private Integer numOfAdults;

    @Column(name = "num_of_children")
    private Integer numOfChildren;

    @Column(name = "total_num_of_guests", nullable = false)
    private Integer numberOfGuests;

    @Column(name = "special_requests", length = 500)
    private String specialRequests;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    /**
     * Tính tổng số khách dựa trên số người lớn và trẻ em
     */
    public void calculateTotalNumberOfGuest() {
        this.numberOfGuests = (this.numOfAdults != null ? this.numOfAdults : 0) +
                (this.numOfChildren != null ? this.numOfChildren : 0);
    }

    @PrePersist
    protected void onCreate() {
        if (this.bookingDate == null) {
            this.bookingDate = LocalDateTime.now();
        }
        calculateTotalNumberOfGuest();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalNumberOfGuest();
    }

    /**
     * Kiểm tra xem ngày trả phòng có sau ngày nhận phòng không
     * 
     * @return true nếu ngày trả phòng hợp lệ
     */
    public boolean isValidDates() {
        return checkOutDate.isAfter(checkInDate);
    }

    /**
     * Kiểm tra xem phòng có còn trống không
     * 
     * @return true nếu phòng còn trống
     */
    public boolean isRoomAvailable() {
        return room != null && room.isAvailable();
    }
}
