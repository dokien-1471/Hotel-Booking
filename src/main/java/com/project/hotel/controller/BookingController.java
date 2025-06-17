package com.project.hotel.controller;

import com.project.hotel.dto.BookingDTO;
import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentService;
import com.project.hotel.service.VNPayService;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.User;
import com.project.hotel.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final VNPayService vnPayService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService, PaymentService paymentService, VNPayService vnPayService,
            UserService userService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingDTO bookingDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        log.info("Received booking request: {}", bookingDTO);

        try {
            // Get user by email from security context
            String userEmail = userDetails.getUsername();
            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
            bookingDTO.setUserId(user.getId());

            // Validate request body
            if (bookingDTO.getRoomId() == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Room ID is required"));
            }

            // Validate binding result
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                log.warn("Validation errors: {}", errors);
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "message", "Validation failed",
                                "errors", errors));
            }

            // Check if room exists and is available
            if (!bookingService.isRoomAvailable(
                    bookingDTO.getRoomId(),
                    bookingDTO.getCheckInDate(),
                    bookingDTO.getCheckOutDate())) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Room is not available for selected dates"));
            }

            // Create booking
            BookingDTO booking = bookingService.createBooking(bookingDTO);
            log.info("Created booking: {}", booking);

            // Generate payment URL
            String paymentUrl = vnPayService.createPaymentUrl(booking.getId(), request);
            log.info("Generated payment URL for booking {}: {}", booking.getId(), paymentUrl);

            return ResponseEntity.ok(Map.of(
                    "bookingId", booking.getId().toString(),
                    "paymentUrl", paymentUrl));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: ", e);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating booking: ", e);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Failed to create booking: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookingDTO booking = bookingService.getBookingById(id);
        // Only allow users to view their own bookings or admin to view any booking
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !booking.getUserId().toString().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BookingDTO>> getBookingsByUserId(@PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Only allow users to view their own bookings or admin to view any booking
            if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                    && !userId.toString().equals(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            log.info("Fetching bookings for user ID: {}", userId);
            List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
            log.info("Found {} bookings for user ID: {}", bookings.size(), userId);

            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            log.error("Error fetching bookings for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getBookingsByRoomId(@PathVariable Long roomId) {
        List<BookingDTO> bookings = bookingService.getBookingsByRoomId(roomId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getBookingsByStatus(@PathVariable String status) {
        List<BookingDTO> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getBookingsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<BookingDTO> bookings = bookingService.getBookingsInDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BookingDTO updatedBooking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingDTO bookingDTO) {
        BookingDTO updatedBooking = bookingService.updateBooking(id, bookingDTO);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingDTO> getBookingByReference(@PathVariable String reference) {
        BookingDTO booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<Map<String, String>> initiatePayment(
            @PathVariable Long id,
            @RequestParam String paymentMethod,
            HttpServletRequest request) {
        if (!"VNPAY".equalsIgnoreCase(paymentMethod)) {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }

        // Get booking information
        BookingDTO booking = bookingService.getBookingById(id);
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found with id: " + id);
        }

        // Create VNPay request
        VNPayRequestDTO vnPayRequest = new VNPayRequestDTO();
        vnPayRequest.setBookingId(id);
        vnPayRequest.setOrderId("ORDER_" + System.currentTimeMillis());
        vnPayRequest.setAmount(booking.getTotalPrice().longValue());
        vnPayRequest.setOrderInfo("Thanh toan dat phong " + booking.getBookingReference());
        vnPayRequest.setIpAddress(getClientIp(request));

        try {
            // Create payment record first
            PaymentDTO paymentDTO = paymentService.processPayment(id, paymentMethod);

            // Generate VNPay payment URL
            String paymentUrl = vnPayService.createPaymentUrl(vnPayRequest);

            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("paymentId", paymentDTO.getId().toString());
            response.put("bookingReference", booking.getBookingReference());
            response.put("amount", booking.getTotalPrice().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingDTO> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookingDTO booking = bookingService.getBookingById(id);

        // Only allow users to cancel their own bookings or admin to cancel any booking
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !booking.getUserId().toString().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BookingDTO canceledBooking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(canceledBooking);
    }
}
