package com.project.hotel.controller;

import com.project.hotel.service.BookingService;
import com.project.hotel.service.RoomService;
import com.project.hotel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

  private final BookingService bookingService;
  private final RoomService roomService;
  private final UserService userService;

  @GetMapping("/dashboard/stats")
  public ResponseEntity<Map<String, Object>> getDashboardStats() {
    Map<String, Object> stats = new HashMap<>();

    stats.put("totalBookings", bookingService.getAllBookings().size());
    stats.put("totalUsers", userService.getAllUsers().size());
    stats.put("availableRooms", roomService.getAvailableRooms().size());

    // Calculate total revenue from all bookings
    BigDecimal totalRevenue = bookingService.getAllBookings().stream()
        .map(booking -> booking.getTotalPrice())
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    stats.put("totalRevenue", totalRevenue);

    return ResponseEntity.ok(stats);
  }
}