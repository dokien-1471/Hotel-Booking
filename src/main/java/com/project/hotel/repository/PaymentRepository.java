package com.project.hotel.repository;

import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByBooking(Booking booking);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByStatus(String status);

    List<Payment> findByMethod(String method);

    Optional<Payment> findByBookingId(Long bookingId);
}
