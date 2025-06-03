package com.project.hotel.service;

import com.project.hotel.dto.BookingDTO;
import com.project.hotel.entity.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);

    BookingDTO getBookingById(Long id);

    List<BookingDTO> getAllBookings();

    List<BookingDTO> getBookingsByUserId(Long userId);

    List<BookingDTO> getBookingsByRoomId(Long roomId);

    List<BookingDTO> getBookingsByStatus(String status);

    List<BookingDTO> getBookingsInDateRange(LocalDate startDate, LocalDate endDate);

    BookingDTO updateBookingStatus(Long id, String status);

    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    void deleteBooking(Long id);

    BookingDTO getBookingByReference(String reference);

    Booking findBookingEntityById(Long bookingId);

    /**
     * Cancels a booking and handles related business logic
     * 
     * @param id The ID of the booking to cancel
     * @return The canceled booking DTO
     * @throws RuntimeException if booking cannot be canceled
     */
    BookingDTO cancelBooking(Long id);

    /**
     * Confirms a booking after successful payment
     * 
     * @param id The ID of the booking to confirm
     * @return The confirmed booking DTO
     */
    BookingDTO confirmBooking(Long id);

    /**
     * Checks if a booking can be cancelled
     * 
     * @param id The ID of the booking to check
     * @return true if the booking can be cancelled, false otherwise
     */
    boolean canCancelBooking(Long id);

    /**
     * Gets the current status of a booking
     * 
     * @param id The ID of the booking
     * @return The current status of the booking
     */
    String getBookingStatus(Long id);
}