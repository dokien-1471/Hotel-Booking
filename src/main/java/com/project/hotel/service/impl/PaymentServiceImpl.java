package com.project.hotel.service.impl;

import com.project.hotel.dto.PaymentDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.exception.BadRequestException;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.BookingService;
import com.project.hotel.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingService bookingService) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
    }

    @Override
    @Transactional
    public PaymentDTO processPayment(Long bookingId, String paymentMethod) {
        Booking booking = bookingService.findBookingEntityById(bookingId);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setMethod(paymentMethod);
        payment.setStatus("PENDING");
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    @Override
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Payment payment = new Payment();
        payment.setAmount(paymentDTO.getAmount());
        payment.setMethod(paymentDTO.getMethod());
        payment.setStatus(paymentDTO.getStatus());
        payment.setPaymentTime(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    @Override
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found"));

        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    @Override
    public Payment findPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Payment not found"));
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setPaymentTime(payment.getPaymentTime());
        if (payment.getBooking() != null) {
            dto.setBookingId(payment.getBooking().getId());
        }
        return dto;
    }
}
