package com.project.hotel.controller;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.service.PaymentNotificationService;
import com.project.hotel.service.PaymentService;
import com.project.hotel.service.VNPayService;
import com.project.hotel.util.PaymentGatewayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentNotificationService paymentNotificationService;
  private final PaymentGatewayUtil paymentGatewayUtil;
  private final VNPayService vnPayService;

  @Autowired
  public PaymentController(PaymentService paymentService,
      PaymentNotificationService paymentNotificationService,
      PaymentGatewayUtil paymentGatewayUtil,
      VNPayService vnPayService) {
    this.paymentService = paymentService;
    this.paymentNotificationService = paymentNotificationService;
    this.paymentGatewayUtil = paymentGatewayUtil;
    this.vnPayService = vnPayService;
  }

  @PostMapping
  public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
    PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
    return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
    PaymentDTO payment = paymentService.getPaymentById(id);
    return ResponseEntity.ok(payment);
  }

  @GetMapping
  public ResponseEntity<List<PaymentDTO>> getAllPayments() {
    List<PaymentDTO> payments = paymentService.getAllPayments();
    return ResponseEntity.ok(payments);
  }

  @GetMapping("/booking/{bookingId}")
  public ResponseEntity<PaymentDTO> getPaymentByBookingId(@PathVariable Long bookingId) {
    PaymentDTO payment = paymentService.getPaymentByBookingId(bookingId);
    return ResponseEntity.ok(payment);
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<PaymentDTO> updatePaymentStatus(
      @PathVariable Long id,
      @RequestParam String status) {
    PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, status);
    return ResponseEntity.ok(updatedPayment);
  }

  @PostMapping("/process")
  public ResponseEntity<PaymentDTO> processPayment(
      @RequestParam Long bookingId,
      @RequestParam String paymentMethod) {
    PaymentDTO processedPayment = paymentService.processPayment(bookingId, paymentMethod);
    return ResponseEntity.ok(processedPayment);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
    paymentService.deletePayment(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/reference/{reference}")
  public ResponseEntity<PaymentDTO> getPaymentByReference(@PathVariable String reference) {
    PaymentDTO payment = paymentService.getPaymentByReference(reference);
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/transaction/{transactionId}")
  public ResponseEntity<PaymentDTO> getPaymentByTransactionId(@PathVariable String transactionId) {
    PaymentDTO payment = paymentService.getPaymentByTransactionId(transactionId);
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable String status) {
    List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
    return ResponseEntity.ok(payments);
  }

  @GetMapping("/method/{method}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByMethod(@PathVariable String method) {
    List<PaymentDTO> payments = paymentService.getPaymentsByMethod(method);
    return ResponseEntity.ok(payments);
  }

  @PostMapping("/notification")
  public ResponseEntity<PaymentDTO> handlePaymentNotification(@RequestBody Map<String, String> notification) {
    String transactionId = notification.get("transactionId");
    String status = notification.get("status");
    String gatewayReference = notification.get("gatewayReference");

    if (transactionId == null || status == null) {
      return ResponseEntity.badRequest().build();
    }

    PaymentDTO updatedPayment = paymentNotificationService.processPaymentNotification(
        transactionId, status, gatewayReference);

    return ResponseEntity.ok(updatedPayment);
  }

  @PostMapping("/{id}/simulate-notification")
  public ResponseEntity<PaymentDTO> simulatePaymentNotification(
      @PathVariable Long id,
      @RequestParam String status) {
    PaymentDTO updatedPayment = paymentNotificationService.simulatePaymentNotification(id, status);
    return ResponseEntity.ok(updatedPayment);
  }

  @GetMapping("/methods")
  public ResponseEntity<String[]> getAvailablePaymentMethods() {
    return ResponseEntity.ok(new String[] { "VNPAY", "CREDIT_CARD", "BANK_TRANSFER" });
  }

  @PostMapping("/vnpay/create")
  public ResponseEntity<String> createVNPayPayment(@RequestBody VNPayRequest request) {
    String paymentUrl = vnPayService.createPaymentUrl(
        request.getOrderId(),
        request.getAmount(),
        request.getOrderInfo());
    return ResponseEntity.ok(paymentUrl);
  }

  @GetMapping("/vnpay/return")
  public ResponseEntity<Map<String, String>> vnPayReturn(@RequestParam Map<String, String> queryParams) {
    boolean isValid = vnPayService.validatePaymentResponse(queryParams);
    if (isValid) {
      return ResponseEntity.ok(queryParams);
    }
    return ResponseEntity.badRequest().body(queryParams);
  }
}

class VNPayRequest {
  private String orderId;
  private long amount;
  private String orderInfo;

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
