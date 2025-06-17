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
import com.project.hotel.constant.PaymentStatus;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.exception.ValidationException;
import com.project.hotel.exception.BookingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final RoomService roomService;

    @Override
    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        try {
            validateBookingDTO(bookingDTO);
            validateBookingDates(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

            User user = userService.findUserEntityById(bookingDTO.getUserId());
            Room room = roomService.findRoomEntityById(bookingDTO.getRoomId());

            validateRoomAvailability(room, bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

            BigDecimal totalPrice = calculateTotalPrice(room, bookingDTO.getCheckInDate(),
                    bookingDTO.getCheckOutDate());

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRoom(room);
            booking.setCheckInDate(bookingDTO.getCheckInDate());
            booking.setCheckOutDate(bookingDTO.getCheckOutDate());
            booking.setTotalPrice(totalPrice);
            booking.setStatus(BookingStatus.PENDING);
            booking.setBookingReference(generateBookingReference());
            booking.setBookingDate(LocalDateTime.now());
            booking.setGuestFullName(bookingDTO.getGuestFullName());
            booking.setGuestEmail(bookingDTO.getGuestEmail());
            booking.setNumOfAdults(bookingDTO.getNumOfAdults());
            booking.setNumOfChildren(bookingDTO.getNumOfChildren());
            booking.calculateTotalNumberOfGuest();

            Booking savedBooking = bookingRepository.save(booking);
            updateRoomAvailability(room, false);

            log.info("Created new booking with reference: {}", savedBooking.getBookingReference());
            return convertToDTO(savedBooking);
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage(), e);
            throw new BookingException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            return convertToDTO(booking);
        } catch (Exception e) {
            log.error("Error getting booking by id {}: {}", id, e.getMessage(), e);
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        try {
            return bookingRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all bookings: {}", e.getMessage(), e);
            throw new BookingException("Failed to get bookings: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        try {
            User user = userService.findUserEntityById(userId);
            return bookingRepository.findByUser(user).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting bookings for user {}: {}", userId, e.getMessage(), e);
            throw new BookingException("Failed to get user bookings: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByRoomId(Long roomId) {
        try {
            Room room = roomService.findRoomEntityById(roomId);
            return bookingRepository.findByRoom(room).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting bookings for room {}: {}", roomId, e.getMessage(), e);
            throw new BookingException("Failed to get room bookings: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsByStatus(String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            return bookingRepository.findByStatus(bookingStatus).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid booking status: " + status);
        } catch (Exception e) {
            log.error("Error getting bookings by status {}: {}", status, e.getMessage(), e);
            throw new BookingException("Failed to get bookings by status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> getBookingsInDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            validateDateRange(startDate, endDate);
            return bookingRepository.findBookingsWithCheckInOrCheckOutInRange(startDate, endDate).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting bookings in date range: {}", e.getMessage(), e);
            throw new BookingException("Failed to get bookings in date range: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long id, String status) {
        try {
            Booking booking = findBookingEntityById(id);
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());

            validateStatusTransition(booking.getStatus(), newStatus);
            booking.setStatus(newStatus);

            if (newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.COMPLETED) {
                updateRoomAvailability(booking.getRoom(), true);
            }

            Booking updatedBooking = bookingRepository.save(booking);
            log.info("Updated booking {} status to {}", id, status);
            return convertToDTO(updatedBooking);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid booking status: " + status);
        } catch (Exception e) {
            log.error("Error updating booking status: {}", e.getMessage(), e);
            throw new BookingException("Failed to update booking status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        try {
            Booking booking = findBookingEntityById(id);
            validateBookingDTO(bookingDTO);
            validateBookingDates(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

            if (!booking.getCheckInDate().equals(bookingDTO.getCheckInDate()) ||
                    !booking.getCheckOutDate().equals(bookingDTO.getCheckOutDate())) {
                validateRoomAvailability(booking.getRoom(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
                booking.setCheckInDate(bookingDTO.getCheckInDate());
                booking.setCheckOutDate(bookingDTO.getCheckOutDate());
                booking.setTotalPrice(calculateTotalPrice(booking.getRoom(),
                        bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate()));
            }

            updateBookingFields(booking, bookingDTO);
            Booking updatedBooking = bookingRepository.save(booking);
            log.info("Updated booking {}", id);
            return convertToDTO(updatedBooking);
        } catch (Exception e) {
            log.error("Error updating booking: {}", e.getMessage(), e);
            throw new BookingException("Failed to update booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            updateRoomAvailability(booking.getRoom(), true);
            bookingRepository.deleteById(id);
            log.info("Deleted booking {}", id);
        } catch (Exception e) {
            log.error("Error deleting booking: {}", e.getMessage(), e);
            throw new BookingException("Failed to delete booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingByReference(String reference) {
        try {
            Booking booking = bookingRepository.findByBookingReference(reference)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));
            return convertToDTO(booking);
        } catch (Exception e) {
            log.error("Error getting booking by reference: {}", e.getMessage(), e);
            throw new BookingException("Failed to get booking by reference: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findBookingEntityById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional
    public BookingDTO cancelBooking(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            validateCancellation(booking);

            booking.setStatus(BookingStatus.CANCELLED);
            updateRoomAvailability(booking.getRoom(), true);

            if (booking.getPayment() != null && "SUCCESS".equals(booking.getPayment().getStatus())) {
                handleRefund(booking.getPayment());
            }

            Booking canceledBooking = bookingRepository.save(booking);
            log.info("Cancelled booking {}", id);
            return convertToDTO(canceledBooking);
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", e.getMessage(), e);
            throw new BookingException("Failed to cancel booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookingDTO confirmBooking(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            validateConfirmation(booking);

            booking.setStatus(BookingStatus.CONFIRMED);
            Booking confirmedBooking = bookingRepository.save(booking);
            log.info("Confirmed booking {}", id);
            return convertToDTO(confirmedBooking);
        } catch (Exception e) {
            log.error("Error confirming booking: {}", e.getMessage(), e);
            throw new BookingException("Failed to confirm booking: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancelBooking(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            return !booking.getStatus().equals(BookingStatus.CANCELLED) &&
                    !booking.getStatus().equals(BookingStatus.CONFIRMED) &&
                    !LocalDate.now().plusDays(1).isAfter(booking.getCheckInDate());
        } catch (Exception e) {
            log.error("Error checking if booking can be cancelled: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getBookingStatus(Long id) {
        try {
            Booking booking = findBookingEntityById(id);
            return booking.getStatus().name();
        } catch (Exception e) {
            log.error("Error getting booking status: {}", e.getMessage(), e);
            throw new BookingException("Failed to get booking status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        // Kiểm tra xem phòng có tồn tại không
        Room room = roomService.findRoomEntityById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId);
        }

        // Kiểm tra xem phòng có đang được đặt trong khoảng thời gian này không
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut);

        // Phòng khả dụng nếu không có booking nào trong khoảng thời gian này
        return overlappingBookings.isEmpty();
    }

    private void validateBookingDTO(BookingDTO bookingDTO) {
        if (bookingDTO == null) {
            throw new ValidationException("Booking data cannot be null");
        }
        if (bookingDTO.getUserId() == null) {
            throw new ValidationException("User ID is required");
        }
        if (bookingDTO.getRoomId() == null) {
            throw new ValidationException("Room ID is required");
        }
        if (bookingDTO.getCheckInDate() == null) {
            throw new ValidationException("Check-in date is required");
        }
        if (bookingDTO.getCheckOutDate() == null) {
            throw new ValidationException("Check-out date is required");
        }
        if (bookingDTO.getNumOfAdults() == null || bookingDTO.getNumOfAdults() <= 0) {
            throw new ValidationException("Number of adults must be greater than 0");
        }
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new ValidationException("Check-in date must be before check-out date");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("Check-in date must be in the future");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
    }

    private void validateRoomAvailability(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) {
            throw new ValidationException("Room is not available");
        }

        List<Booking> existingBookings = bookingRepository.findBookingsWithCheckInOrCheckOutInRange(checkIn, checkOut);
        boolean roomAlreadyBooked = existingBookings.stream()
                .anyMatch(booking -> booking.getRoom().getId().equals(room.getId()) &&
                        !booking.getStatus().equals(BookingStatus.CANCELLED));

        if (roomAlreadyBooked) {
            throw new ValidationException("Room is already booked for the selected dates");
        }
    }

    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        if (currentStatus == BookingStatus.CANCELLED) {
            throw new ValidationException("Cannot change status of a cancelled booking");
        }
        if (currentStatus == BookingStatus.CONFIRMED && newStatus == BookingStatus.PENDING) {
            throw new ValidationException("Cannot change confirmed booking back to pending");
        }
    }

    private void validateCancellation(Booking booking) {
        if (booking.getStatus().equals(BookingStatus.CANCELLED)) {
            throw new ValidationException("Booking is already cancelled");
        }
        if (booking.getStatus().equals(BookingStatus.CONFIRMED)) {
            throw new ValidationException("Cannot cancel a confirmed booking");
        }
        if (LocalDate.now().plusDays(1).isAfter(booking.getCheckInDate())) {
            throw new ValidationException("Booking cannot be cancelled within 24 hours of check-in");
        }
    }

    private void validateConfirmation(Booking booking) {
        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new ValidationException("Only pending bookings can be confirmed");
        }
    }

    private void updateBookingFields(Booking booking, BookingDTO bookingDTO) {
        if (bookingDTO.getGuestFullName() != null) {
            booking.setGuestFullName(bookingDTO.getGuestFullName());
        }
        if (bookingDTO.getGuestEmail() != null) {
            booking.setGuestEmail(bookingDTO.getGuestEmail());
        }
        if (bookingDTO.getNumOfAdults() != null) {
            booking.setNumOfAdults(bookingDTO.getNumOfAdults());
        }
        if (bookingDTO.getNumOfChildren() != null) {
            booking.setNumOfChildren(bookingDTO.getNumOfChildren());
        }
        booking.calculateTotalNumberOfGuest();
    }

    private void updateRoomAvailability(Room room, boolean available) {
        room.setAvailable(available);
        roomService.updateRoom(room.getId(), convertRoomToDTO(room));
    }

    private void handleRefund(Payment payment) {
        // Implement refund logic here
        payment.setStatus(PaymentStatus.REFUNDED);
        // Add additional refund processing as needed
    }

    private String generateBookingReference() {
        return "HB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-"
                + System.currentTimeMillis() % 10000;
    }

    private BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long numberOfDays = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (numberOfDays == 0) {
            numberOfDays = 1;
        }
        return room.getPrice().multiply(BigDecimal.valueOf(numberOfDays));
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoom(convertRoomToDTO(booking.getRoom()));
        dto.setBookingReference(booking.getBookingReference());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus().name());
        dto.setBookingDate(booking.getBookingDate());
        dto.setGuestFullName(booking.getGuestFullName());
        dto.setGuestEmail(booking.getGuestEmail());
        dto.setNumOfAdults(booking.getNumOfAdults());
        dto.setNumOfChildren(booking.getNumOfChildren());
        dto.setTotalNumberOfGuest(booking.getNumberOfGuests());
        return dto;
    }

    private RoomDTO convertRoomToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setRoomNumber(room.getRoomNumber());
        dto.setRoomType(room.getRoomType());
        dto.setType(room.getRoomType());
        dto.setPrice(room.getPrice());
        dto.setAvailable(room.isAvailable());
        dto.setDescription(room.getDescription());
        dto.setImages(room.getImages());
        dto.setAmenities(room.getAmenities());
        dto.setName("Phòng " + room.getRoomNumber());
        return dto;
    }
}