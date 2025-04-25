package com.project.hotel.repository;

import com.project.hotel.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByBookingId(Long bookingId);

  List<Review> findByUserId(Long userId);

  List<Review> findByRoomId(Long roomId);

  boolean existsByBookingId(Long bookingId);
}