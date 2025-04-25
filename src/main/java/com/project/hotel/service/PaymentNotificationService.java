package com.project.hotel.service;

import com.project.hotel.dto.PaymentDTO;

/**
 * Service for handling payment notifications and callbacks from payment gateways.
 * In a real application, this would handle webhook callbacks from payment providers.
 */
public interface PaymentNotificationService {
    
    /**
     * Process a payment notification from a payment gateway.
     * 
     * @param transactionId The transaction ID from the payment gateway
     * @param status The payment status (SUCCESS, FAILED)
     * @param gatewayReference The payment gateway's reference ID
     * @return The updated payment information
     */
    PaymentDTO processPaymentNotification(String transactionId, String status, String gatewayReference);
    
    /**
     * Simulate a payment notification for testing purposes.
     * 
     * @param paymentId The ID of the payment to update
     * @param status The new payment status
     * @return The updated payment information
     */
    PaymentDTO simulatePaymentNotification(Long paymentId, String status);
}
