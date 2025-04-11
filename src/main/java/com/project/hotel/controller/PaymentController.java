package com.project.hotel.controller;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.service.PaymentNotificationService;
import com.project.hotel.service.PaymentService;
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

    @Autowired
    public PaymentController(PaymentService paymentService, 
                            PaymentNotificationService paymentNotificationService,
                            PaymentGatewayUtil paymentGatewayUtil) {
        this.paymentService = paymentService;
        this.paymentNotificationService = paymentNotificationService;
        this.paymentGatewayUtil = paymentGatewayUtil;
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
    
    /**
     * Endpoint for payment gateway callbacks/webhooks.
     * In a real application, this would be called by the payment gateway when a payment status changes.
     */
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
    
    /**
     * Endpoint to simulate payment notifications for testing.
     */
    @PostMapping("/{id}/simulate-notification")
    public ResponseEntity<PaymentDTO> simulatePaymentNotification(
            @PathVariable Long id,
            @RequestParam String status) {
        PaymentDTO updatedPayment = paymentNotificationService.simulatePaymentNotification(id, status);
        return ResponseEntity.ok(updatedPayment);
    }
    
    /**
     * Get available payment methods.
     */
    @GetMapping("/methods")
    public ResponseEntity<String[]> getAvailablePaymentMethods() {
        return ResponseEntity.ok(paymentGatewayUtil.getSupportedPaymentMethods());
    }
}
