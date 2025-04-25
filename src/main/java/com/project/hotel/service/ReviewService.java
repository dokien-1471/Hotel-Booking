package com.project.hotel.service;

import com.project.hotel.dto.ReviewDTO;
import java.util.List;

public interface ReviewService {
  ReviewDTO createReview(ReviewDTO reviewDTO);

  ReviewDTO getReviewById(Long id);

  List<ReviewDTO> getReviewsByBookingId(Long bookingId);

  List<ReviewDTO> getReviewsByUserId(Long userId);

  List<ReviewDTO> getReviewsByRoomId(Long roomId);

  ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);

  void deleteReview(Long id);

  boolean hasUserReviewedBooking(Long bookingId, Long userId);
}