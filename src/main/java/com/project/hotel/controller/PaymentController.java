package com.project.hotel.controller;

import com.project.hotel.service.VNPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

  private final VNPayService vnPayService;

  public PaymentController(VNPayService vnPayService) {
    this.vnPayService = vnPayService;
  }

  @PostMapping("/create")
  public ResponseEntity<String> createPayment(@RequestBody PaymentRequest request) {
    String paymentUrl = vnPayService.createPaymentUrl(
        request.getOrderId(),
        request.getAmount(),
        request.getOrderInfo());
    return ResponseEntity.ok(paymentUrl);
  }

  @GetMapping("/return")
  public ResponseEntity<Map<String, String>> paymentReturn(@RequestParam Map<String, String> queryParams) {
    boolean isValid = vnPayService.validatePaymentResponse(queryParams);
    if (isValid) {
      return ResponseEntity.ok(queryParams);
    }
    return ResponseEntity.badRequest().body(queryParams);
  }
}

class PaymentRequest {
  private String orderId;
  private long amount;
  private String orderInfo;

  // Getters and Setters
  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public String getOrderInfo() {
    return orderInfo;
  }

  public void setOrderInfo(String orderInfo) {
    this.orderInfo = orderInfo;
  }
}