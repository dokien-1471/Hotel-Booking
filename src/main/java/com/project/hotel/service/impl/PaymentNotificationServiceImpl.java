package com.project.hotel.service.impl;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Payment;
import com.project.hotel.exception.BadRequestException;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentNotificationServiceImpl implements PaymentNotificationService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Autowired
    public PaymentNotificationServiceImpl(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @Override
    public PaymentDTO processPaymentNotification(String transactionId, String status, String gatewayReference) {
        // Find payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new BadRequestException("Payment not found with transaction ID: " + transactionId);
        }
        
       
        if (!isValidStatus(status)) {
            throw new BadRequestException("Invalid payment status: " + status);
        }
        
        
        payment.setStatus(status);
        
        
        if (status.equals("SUCCESS")) {
            bookingService.updateBookingStatus(payment.getBooking().getId(), "CONFIRMED");
        } else if (status.equals("FAILED")) {
            bookingService.updateBookingStatus(payment.getBooking().getId(), "CANCELLED");
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        
        return convertToDTO(updatedPayment);
    }

    @Override
    public PaymentDTO simulatePaymentNotification(Long paymentId, String status) {
       
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found with id: " + paymentId));
        
        
        if (!isValidStatus(status)) {
            throw new BadRequestException("Invalid payment status: " + status);
        }
        
        
        payment.setStatus(status);
        
        
        if (status.equals("SUCCESS")) {
            bookingService.updateBookingStatus(payment.getBooking().getId(), "CONFIRMED");
        } else if (status.equals("FAILED")) {
            bookingService.updateBookingStatus(payment.getBooking().getId(), "CANCELLED");
        }
        
       
        Payment updatedPayment = paymentRepository.save(payment);
        
        return convertToDTO(updatedPayment);
    }
    
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("PENDING") || status.equals("SUCCESS") || status.equals("FAILED"));
    }
    
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setAmount(payment.getAmount());
        paymentDTO.setPaymentTime(payment.getPaymentTime());
        paymentDTO.setMethod(payment.getMethod());
        paymentDTO.setStatus(payment.getStatus());
        paymentDTO.setBookingId(payment.getBooking().getId());
        paymentDTO.setBookingReference(payment.getBooking().getBookingReference());
        paymentDTO.setTransactionId(payment.getTransactionId());
        paymentDTO.setPaymentReference(payment.getPaymentReference());
        
        
        paymentDTO.setUserFullName(payment.getBooking().getUser().getFirstName() + " " + 
                payment.getBooking().getUser().getLastName());
        paymentDTO.setRoomNumber(payment.getBooking().getRoom().getRoomNumber());
        
        return paymentDTO;
    }
}
