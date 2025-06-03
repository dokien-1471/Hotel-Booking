package com.project.hotel.controller;

import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.dto.VNPayResponseDTO;
import com.project.hotel.service.PaymentService;
import com.project.hotel.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VNPayController {

  private final VNPayService vnPayService;
  private final PaymentService paymentService;

  @PostMapping("/create-payment")
  public ResponseEntity<Map<String, String>> createPayment(@RequestBody VNPayRequestDTO request) {
    String paymentUrl = vnPayService.createPaymentUrl(request);
    Map<String, String> response = new HashMap<>();
    response.put("paymentUrl", paymentUrl);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/payment-callback")
  public ResponseEntity<Map<String, String>> paymentCallback(
      @RequestParam("vnp_ResponseCode") String responseCode,
      @RequestParam("vnp_TxnRef") String txnRef,
      @RequestParam("vnp_TransactionNo") String transactionNo) {

    VNPayResponseDTO response = vnPayService.processPaymentResponse(responseCode, txnRef, transactionNo);

    Map<String, String> result = new HashMap<>();
    if ("00".equals(response.getResponseCode())) {
      result.put("status", "success");
      result.put("message", "Payment successful");
    } else {
      result.put("status", "failed");
      result.put("message", "Payment failed");
    }

    return ResponseEntity.ok(result);
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}