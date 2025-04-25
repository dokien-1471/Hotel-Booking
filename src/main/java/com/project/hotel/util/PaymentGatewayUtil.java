package com.project.hotel.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for simulating payment gateway integration.
 * In a real application, this would be replaced with actual payment gateway API calls.
 */
@Component
public class PaymentGatewayUtil {

    /**
     * Simulates processing a payment through a payment gateway.
     * 
     * @param amount The amount to be charged
     * @param paymentMethod The payment method (VNPAY, MOMO, CREDIT_CARD, etc.)
     * @param reference A reference for the payment
     * @return A transaction ID if successful, null if failed
     */
    public String processPayment(java.math.BigDecimal amount, String paymentMethod, String reference) {
        // Simulate payment processing
        // In a real application, this would call an external payment API
        
        try {
            // Simulate processing time
            Thread.sleep(1000);
            
            // Simulate success rate (90% success)
            double random = Math.random();
            if (random < 0.9) {
                // Generate a transaction ID
                return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            } else {
                // Simulate payment failure
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * Validates if the payment method is supported.
     * 
     * @param method The payment method to validate
     * @return true if the method is supported, false otherwise
     */
    public boolean isValidPaymentMethod(String method) {
        return method != null && (
                method.equals("VNPAY") || 
                method.equals("MOMO") || 
                method.equals("COD") || 
                method.equals("CREDIT_CARD") || 
                method.equals("BANK_TRANSFER"));
    }
    
    /**
     * Gets the list of supported payment methods.
     * 
     * @return Array of supported payment methods
     */
    public String[] getSupportedPaymentMethods() {
        return new String[] {"VNPAY", "MOMO", "COD", "CREDIT_CARD", "BANK_TRANSFER"};
    }
}
