package com.project.hotel.service.impl;

import com.project.hotel.dto.BookingDTO;
import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Room;
import com.project.hotel.entity.User;
import com.project.hotel.entity.Payment;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.RoomService;
import com.project.hotel.service.UserService;
import com.project.hotel.constant.BookingStatus;
import com.project.hotel.constant.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final RoomService roomService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, UserService userService, RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.roomService = roomService;
    }

    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        // Validate check-in and check-out dates
        if (bookingDTO.getCheckInDate().isAfter(bookingDTO.getCheckOutDate())) {
            throw new RuntimeException("Check-in date cannot be after check-out date");
        }

        // Get user and room entities
        User user = userService.findUserEntityById(bookingDTO.getUserId());
        Room room = roomService.findRoomEntityById(bookingDTO.getRoomId());

        // Check if room is available
        if (!room.isAvailable()) {
            throw new RuntimeException("Room is not available for booking");
        }

        // Check if room is already booked for the requested dates
        List<Booking> existingBookings = bookingRepository.findBookingsInDateRange(
                bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        boolean roomAlreadyBooked = existingBookings.stream()
                .anyMatch(booking -> booking.getRoom().getId().equals(room.getId()));

        if (roomAlreadyBooked) {
            throw new RuntimeException("Room is already booked for the selected dates");
        }

        // Calculate total price based on number of days and room price
        long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (days == 0)
            days = 1; // Minimum 1 day charge

        // Create booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setTotalPrice(room.getPrice().multiply(java.math.BigDecimal.valueOf(days)));
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingReference(generateBookingReference());
        booking.setBookingDate(LocalDateTime.now());

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        // Update room availability
        room.setAvailable(false);
        roomService.updateRoom(room.getId(), convertRoomToDTO(room));

        // Convert saved entity to DTO
        return convertToDTO(savedBooking);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        User user = userService.findUserEntityById(userId);
        return bookingRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByRoomId(Long roomId) {
        Room room = roomService.findRoomEntityById(roomId);
        return bookingRepository.findByRoom(room).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(BookingStatus.valueOf(status)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsInDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findBookingsInDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO updateBookingStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        booking.setStatus(BookingStatus.valueOf(status));

        // If booking is cancelled, make the room available again
        if (status.equals(BookingStatus.CANCELLED.name()) || status.equals(BookingStatus.PAID.name())) {
            Room room = booking.getRoom();
            room.setAvailable(true);
            roomService.updateRoom(room.getId(), convertRoomToDTO(room));
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    @Override
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Validate check-in and check-out dates
        if (bookingDTO.getCheckInDate().isAfter(bookingDTO.getCheckOutDate())) {
            throw new RuntimeException("Check-in date cannot be after check-out date");
        }

        // Check if dates are being changed and if the room is available for those dates
        if (!booking.getCheckInDate().equals(bookingDTO.getCheckInDate()) ||
                !booking.getCheckOutDate().equals(bookingDTO.getCheckOutDate())) {

            List<Booking> existingBookings = bookingRepository.findBookingsInDateRange(
                    bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

            boolean roomAlreadyBooked = existingBookings.stream()
                    .anyMatch(b -> b.getRoom().getId().equals(booking.getRoom().getId()) && !b.getId().equals(id));

            if (roomAlreadyBooked) {
                throw new RuntimeException("Room is already booked for the selected dates");
            }

            // Update dates
            booking.setCheckInDate(bookingDTO.getCheckInDate());
            booking.setCheckOutDate(bookingDTO.getCheckOutDate());

            // Recalculate total price
            long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
            if (days == 0)
                days = 1; // Minimum 1 day charge
            booking.setTotalPrice(booking.getRoom().getPrice().multiply(java.math.BigDecimal.valueOf(days)));
        }

        // Update status if provided
        if (bookingDTO.getStatus() != null) {
            booking.setStatus(bookingDTO.getStatus());

            // If booking is cancelled or completed, make the room available again
            if (bookingDTO.getStatus().equals(BookingStatus.CANCELLED) ||
                    bookingDTO.getStatus().equals(BookingStatus.PAID)) {
                Room room = booking.getRoom();
                room.setAvailable(true);
                roomService.updateRoom(room.getId(), convertRoomToDTO(room));
            }
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Make the room available again
        Room room = booking.getRoom();
        room.setAvailable(true);
        roomService.updateRoom(room.getId(), convertRoomToDTO(room));

        bookingRepository.deleteById(id);
    }

    @Override
    public BookingDTO getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference);
        if (booking == null) {
            throw new RuntimeException("Booking not found with reference: " + reference);
        }
        return convertToDTO(booking);
    }

    @Override
    public Booking findBookingEntityById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Override
    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Check if booking can be canceled
        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new RuntimeException("Booking is already cancelled");
        }

        if (booking.getStatus().equals(BookingStatus.PAID)) {
            throw new RuntimeException("Cannot cancel a completed booking");
        }

        // Check if cancellation is within allowed timeframe (e.g., 24 hours before
        // check-in)
        if (LocalDate.now().plusDays(1).isAfter(booking.getCheckInDate())) {
            throw new RuntimeException("Booking cannot be cancelled within 24 hours of check-in");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);

        // Make the room available again
        Room room = booking.getRoom();
        room.setAvailable(true);
        roomService.updateRoom(room.getId(), convertRoomToDTO(room));

        // If there's a payment, handle refund logic here
        Payment payment = booking.getPayment();
        if (payment != null && payment.getStatus().equals("SUCCESS")) {
            // In a real application, you would integrate with a payment gateway
            // to process the refund
            payment.setStatus("REFUNDED");
            // You might want to create a refund record or update payment history
        }

        // Save the updated booking
        Booking canceledBooking = bookingRepository.save(booking);
        return convertToDTO(canceledBooking);
    }

    @Override
    public BookingDTO confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Check if booking can be confirmed
        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new RuntimeException("Booking cannot be confirmed in current status: " + booking.getStatus());
        }

        // Update booking status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);
        return convertToDTO(confirmedBooking);
    }

    @Override
    public boolean canCancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        // Check if booking is already cancelled
        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            return false;
        }

        // Check if booking is already completed
        if (booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            return false;
        }

        // Check if cancellation is within allowed timeframe (24 hours before check-in)
        return !LocalDate.now().plusDays(1).isAfter(booking.getCheckInDate());
    }

    @Override
    public String getBookingStatus(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return booking.getStatus().name();
    }

    private String generateBookingReference() {
        // Generate a unique booking reference (e.g., HB-UUID-TIMESTAMP)
        return "HB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-"
                + System.currentTimeMillis() % 10000;
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setUserId(booking.getUser().getId());
        bookingDTO.setRoomId(booking.getRoom().getId());
        bookingDTO.setCheckInDate(booking.getCheckInDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingDTO.setTotalPrice(booking.getTotalPrice());
        bookingDTO.setStatus(booking.getStatus());
        bookingDTO.setBookingReference(booking.getBookingReference());
        bookingDTO.setBookingDate(booking.getBookingDate());
        bookingDTO.setSpecialRequests(booking.getSpecialRequests());
        bookingDTO.setNumberOfGuests(booking.getNumberOfGuests());

        // Additional information
        bookingDTO.setUserFullName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        bookingDTO.setRoomNumber(booking.getRoom().getRoomNumber());
        bookingDTO.setRoomType(booking.getRoom().getRoomType());

        return bookingDTO;
    }

    private RoomDTO convertRoomToDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomNumber(room.getRoomNumber());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setPrice(room.getPrice());
        roomDTO.setAvailable(room.isAvailable());
        roomDTO.setDescription(room.getDescription());
        roomDTO.setImages(room.getImages());
        return roomDTO;
    }
}