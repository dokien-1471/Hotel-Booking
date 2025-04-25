package com.project.hotel.service.impl;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.exception.BadRequestException;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentService;
import com.project.hotel.util.PaymentGatewayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final PaymentGatewayUtil paymentGatewayUtil;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingService bookingService, PaymentGatewayUtil paymentGatewayUtil) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.paymentGatewayUtil = paymentGatewayUtil;
    }

    @Override
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        // Get booking entity
        Booking booking = bookingService.findBookingEntityById(paymentDTO.getBookingId());
        
        // Check if payment already exists for this booking
        Payment existingPayment = paymentRepository.findByBooking(booking);
        if (existingPayment != null) {
            throw new BadRequestException("Payment already exists for this booking");
        }
        
        // Create payment entity
        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setMethod(paymentDTO.getMethod());
        payment.setStatus("PENDING"); // Initial status is always PENDING
        payment.setBooking(booking);
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentReference(generatePaymentReference());
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Convert saved entity to DTO
        return convertToDTO(savedPayment);
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = findPaymentEntityById(id);
        return convertToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDTO getPaymentByBookingId(Long bookingId) {
        Booking booking = bookingService.findBookingEntityById(bookingId);
        Payment payment = paymentRepository.findByBooking(booking);
        if (payment == null) {
            throw new BadRequestException("Payment not found for booking with id: " + bookingId);
        }
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO updatePaymentStatus(Long id, String status) {
        Payment payment = findPaymentEntityById(id);
        
        // Validate status
        if (!isValidStatus(status)) {
            throw new BadRequestException("Invalid payment status: " + status);
        }
        
        payment.setStatus(status);
        
        // If payment is successful, update booking status to CONFIRMED
        if (status.equals("SUCCESS")) {
            Booking booking = payment.getBooking();
            bookingService.updateBookingStatus(booking.getId(), "CONFIRMED");
        } else if (status.equals("FAILED")) {
            // If payment failed, update booking status to CANCELLED
            Booking booking = payment.getBooking();
            bookingService.updateBookingStatus(booking.getId(), "CANCELLED");
        }
        
        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    @Override
    public PaymentDTO processPayment(Long bookingId, String paymentMethod) {
        // Get booking
        Booking booking = bookingService.findBookingEntityById(bookingId);
        
        // Check if payment already exists
        Payment existingPayment = paymentRepository.findByBooking(booking);
        if (existingPayment != null) {
            throw new BadRequestException("Payment already exists for this booking");
        }
        
        // Validate payment method
        if (!paymentGatewayUtil.isValidPaymentMethod(paymentMethod)) {
            throw new BadRequestException("Invalid payment method: " + paymentMethod);
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setMethod(paymentMethod);
        payment.setStatus("PENDING");
        payment.setBooking(booking);
        payment.setPaymentReference(generatePaymentReference());
        
        // Save initial pending payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Process payment through payment gateway
        String transactionId = paymentGatewayUtil.processPayment(
                payment.getAmount(), 
                paymentMethod, 
                payment.getPaymentReference());
        
        // Update payment with transaction result
        if (transactionId != null) {
            savedPayment.setTransactionId(transactionId);
            savedPayment.setStatus("SUCCESS");
            
            // Update booking status
            bookingService.updateBookingStatus(booking.getId(), "CONFIRMED");
        } else {
            savedPayment.setStatus("FAILED");
            
            // Update booking status
            bookingService.updateBookingStatus(booking.getId(), "CANCELLED");
        }
        
        savedPayment = paymentRepository.save(savedPayment);
        
        return convertToDTO(savedPayment);
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = findPaymentEntityById(id);
        paymentRepository.deleteById(id);
    }

    @Override
    public PaymentDTO getPaymentByReference(String reference) {
        Payment payment = paymentRepository.findByPaymentReference(reference);
        if (payment == null) {
            throw new BadRequestException("Payment not found with reference: " + reference);
        }
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new BadRequestException("Payment not found with transaction ID: " + transactionId);
        }
        return convertToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByMethod(String method) {
        return paymentRepository.findByMethod(method).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Payment findPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found with id: " + id));
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
        
        // Additional information
        paymentDTO.setUserFullName(payment.getBooking().getUser().getFirstName() + " " + 
                payment.getBooking().getUser().getLastName());
        paymentDTO.setRoomNumber(payment.getBooking().getRoom().getRoomNumber());
        
        return paymentDTO;
    }

    private String generatePaymentReference() {
        // Generate a unique payment reference (e.g., HP-UUID-TIMESTAMP)
        return "HP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis() % 10000;
    }

    private boolean isValidStatus(String status) {
        return status != null && (status.equals("PENDING") || status.equals("SUCCESS") || status.equals("FAILED"));
    }
}
