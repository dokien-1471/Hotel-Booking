package com.project.hotel.service;

import com.project.hotel.dto.BookingDTO;

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
}