package com.project.hotel.service.impl;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.constant.PaymentStatus;
import com.project.hotel.constant.PaymentMethod;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.exception.ValidationException;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Override
    @Transactional
    public PaymentDTO processPayment(Long bookingId, String paymentMethod) {
        try {
            Booking booking = bookingService.findBookingEntityById(bookingId);
            validatePaymentMethod(paymentMethod);

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPrice());
            payment.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentTime(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Created payment for booking {} with status {}", bookingId, payment.getStatus());
            return convertToDTO(savedPayment);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid payment method: " + paymentMethod);
        } catch (Exception e) {
            log.error("Error processing payment for booking {}: {}", bookingId, e.getMessage(), e);
            throw new ValidationException("Failed to process payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        try {
            validatePaymentDTO(paymentDTO);

            Payment payment = new Payment();
            payment.setAmount(paymentDTO.getAmount());
            payment.setMethod(PaymentMethod.valueOf(paymentDTO.getMethod().toUpperCase()));
            payment.setStatus(PaymentStatus.valueOf(paymentDTO.getStatus().toUpperCase()));
            payment.setPaymentTime(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Created payment with status {}", payment.getStatus());
            return convertToDTO(savedPayment);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid payment data: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage(), e);
            throw new ValidationException("Failed to create payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        try {
            Payment payment = findPaymentEntityById(id);
            return convertToDTO(payment);
        } catch (Exception e) {
            log.error("Error getting payment {}: {}", id, e.getMessage(), e);
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, String status) {
        try {
            Payment payment = findPaymentEntityById(id);
            PaymentStatus newStatus = PaymentStatus.valueOf(status.toUpperCase());

            validateStatusTransition(payment.getStatus(), newStatus);
            payment.setStatus(newStatus);

            Payment updatedPayment = paymentRepository.save(payment);
            log.info("Updated payment {} status to {}", id, newStatus);
            return convertToDTO(updatedPayment);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid payment status: " + status);
        } catch (Exception e) {
            log.error("Error updating payment status: {}", e.getMessage(), e);
            throw new ValidationException("Failed to update payment status: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private void validatePaymentDTO(PaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            throw new ValidationException("Payment data cannot be null");
        }
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid payment amount");
        }
        if (paymentDTO.getMethod() == null) {
            throw new ValidationException("Payment method is required");
        }
        if (paymentDTO.getStatus() == null) {
            throw new ValidationException("Payment status is required");
        }
    }

    private void validatePaymentMethod(String method) {
        try {
            PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unsupported payment method: " + method);
        }
    }

    private void validateStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == PaymentStatus.REFUNDED) {
            throw new ValidationException("Cannot change status of a refunded payment");
        }
        if (currentStatus == PaymentStatus.PAID && newStatus == PaymentStatus.PENDING) {
            throw new ValidationException("Cannot change paid payment back to pending");
        }
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentTime(payment.getPaymentTime());
        if (payment.getBooking() != null) {
            dto.setBookingId(payment.getBooking().getId());
        }
        return dto;
    }
}
