package com.project.hotel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long roomId;
    private RoomDTO room;
    private String bookingReference;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime bookingDate;
    private String guestFullName;
    private String guestEmail;
    private Integer numOfAdults;
    private Integer numOfChildren;
    private Integer totalNumberOfGuest;
}