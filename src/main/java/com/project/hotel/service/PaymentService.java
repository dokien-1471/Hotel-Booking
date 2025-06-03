package com.project.hotel.service;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Payment;

public interface PaymentService {
    PaymentDTO createPayment(PaymentDTO paymentDTO);

    PaymentDTO getPaymentById(Long id);

    PaymentDTO updatePaymentStatus(Long id, String status);

    Payment findPaymentEntityById(Long id);

    PaymentDTO processPayment(Long bookingId, String paymentMethod);
}
