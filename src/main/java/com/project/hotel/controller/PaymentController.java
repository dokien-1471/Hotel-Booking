package com.project.hotel.controller;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.service.PaymentService;
import com.project.hotel.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

  private final PaymentService paymentService;
  private final VNPayService vnPayService;

  @Autowired
  public PaymentController(PaymentService paymentService, VNPayService vnPayService) {
    this.paymentService = paymentService;
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

  @PutMapping("/{id}/status")
  public ResponseEntity<PaymentDTO> updatePaymentStatus(
      @PathVariable Long id,
      @RequestParam String status) {
    PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, status);
    return ResponseEntity.ok(updatedPayment);
  }

  @PostMapping("/vnpay/create")
  public ResponseEntity<String> createVNPayPayment(@RequestBody VNPayRequestDTO request) {
    String paymentUrl = vnPayService.createPaymentUrl(request);
    return ResponseEntity.ok(paymentUrl);
  }
}
