package com.project.hotel.service;

import com.project.hotel.dto.BookingDTO;
import com.project.hotel.entity.Booking;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    BookingDTO updateBookingStatus(Long bookingId, String status);

    BookingDTO updateBooking(Long id, BookingDTO bookingDTO);

    void deleteBooking(Long id);

    BookingDTO getBookingByReference(String reference);

    Booking findBookingEntityById(Long bookingId);


    @Transactional
    BookingDTO cancelBooking(Long bookingId);


    @Transactional
    BookingDTO confirmBooking(Long bookingId);


    boolean canCancelBooking(Long id);


    String getBookingStatus(Long id);

    boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut);
}