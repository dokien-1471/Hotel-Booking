package com.project.hotel.service;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Payment;

import java.util.List;

public interface PaymentService {
    
    PaymentDTO createPayment(PaymentDTO paymentDTO);
    
    PaymentDTO getPaymentById(Long id);
    
    List<PaymentDTO> getAllPayments();
    
    PaymentDTO getPaymentByBookingId(Long bookingId);
    
    PaymentDTO updatePaymentStatus(Long id, String status);
    
    PaymentDTO processPayment(Long bookingId, String paymentMethod);
    
    void deletePayment(Long id);
    
    PaymentDTO getPaymentByReference(String reference);
    
    PaymentDTO getPaymentByTransactionId(String transactionId);
    
    List<PaymentDTO> getPaymentsByStatus(String status);
    
    List<PaymentDTO> getPaymentsByMethod(String method);
    
    // Method for internal use to get the entity
    Payment findPaymentEntityById(Long id);
}
