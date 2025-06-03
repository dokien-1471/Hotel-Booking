package com.project.hotel.controller;

import com.project.hotel.dto.BookingDTO;
import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentService;
import com.project.hotel.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    @Autowired
    public BookingController(BookingService bookingService, PaymentService paymentService, VNPayService vnPayService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        BookingDTO createdBooking = bookingService.createBooking(bookingDTO);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
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
        // Only allow users to view their own bookings or admin to view any booking
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !userId.toString().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
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
