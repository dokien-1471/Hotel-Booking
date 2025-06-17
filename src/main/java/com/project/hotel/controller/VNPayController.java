package com.project.hotel.controller;

import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.VNPayService;
import com.project.hotel.dto.VNPayResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VNPayController {

  private final VNPayService vnPayService;
  private final BookingService bookingService;

  @PostMapping("/create-payment/{bookingId}")
  public ResponseEntity<String> createPayment(@PathVariable Long bookingId, HttpServletRequest request) {
    try {
      String paymentUrl = vnPayService.createPaymentUrl(bookingId, request);
      return ResponseEntity.ok(paymentUrl);
    } catch (Exception e) {
      log.error("Error creating payment for booking {}: {}", bookingId, e.getMessage(), e);
      return ResponseEntity.badRequest().body("Error creating payment: " + e.getMessage());
    }
  }

  @PostMapping("/verify")
  public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> vnpParams) {
    try {
      // Validate parameters
      if (!validateVNPayParams(vnpParams)) {
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", "Invalid payment parameters"));
      }

      // Process payment return and update booking status
      Payment payment = vnPayService.processPaymentReturn(vnpParams);

      return ResponseEntity.ok(Map.of(
          "success", true,
          "message", "Payment verified successfully",
          "paymentId", payment.getId(),
          "bookingId", payment.getBooking().getId(),
          "status", payment.getStatus()));
    } catch (Exception e) {
      log.error("Error verifying payment: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", e.getMessage()));
    }
  }

  @GetMapping("/return")
  public RedirectView paymentReturn(HttpServletRequest request) {
    // Get all parameters from VNPay
    Map<String, String> vnpParams = request.getParameterMap().entrySet().stream()
        .collect(java.util.stream.Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue()[0]));

    try {
      // Validate parameters
      if (!validateVNPayParams(vnpParams)) {
        return new RedirectView("/payment-error?message=Invalid payment parameters");
      }

      // Process payment return
      Payment payment = vnPayService.processPaymentReturn(vnpParams);

      // Redirect based on payment status
      if (payment.getStatus().toString().equals("PAID")) {
        return new RedirectView("/payment-success?" + request.getQueryString());
      } else {
        return new RedirectView("/payment-error?message=Payment failed");
      }
    } catch (Exception e) {
      log.error("Error processing payment return: {}", e.getMessage(), e);
      return new RedirectView("/payment-error?message=" + e.getMessage());
    }
  }

  private boolean validateVNPayParams(Map<String, String> params) {
    return params != null &&
        params.containsKey("vnp_ResponseCode") &&
        params.containsKey("vnp_TxnRef") &&
        params.containsKey("vnp_TransactionNo") &&
        params.containsKey("vnp_SecureHash");
  }
}