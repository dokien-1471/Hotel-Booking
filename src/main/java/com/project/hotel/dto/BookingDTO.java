package com.project.hotel.dto;

import com.project.hotel.constant.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;

    private BookingStatus status;

    private String bookingReference;

    private LocalDateTime bookingDate;

    private String specialRequests;

    @Positive(message = "Number of guests must be positive")
    private Integer numberOfGuests;

    // Additional information
    private String userFullName;
    private String roomNumber;
    private String roomType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reference;
    private RoomDTO room;
    private UserDTO user;
}