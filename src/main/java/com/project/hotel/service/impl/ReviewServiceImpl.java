package com.project.hotel.service.impl;

import com.project.hotel.dto.ReviewDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Review;
import com.project.hotel.entity.User;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.repository.ReviewRepository;
import com.project.hotel.repository.UserRepository;
import com.project.hotel.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;

  @Autowired
  public ReviewServiceImpl(ReviewRepository reviewRepository,
      BookingRepository bookingRepository,
      UserRepository userRepository) {
    this.reviewRepository = reviewRepository;
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
  }

  @Override
  public ReviewDTO createReview(ReviewDTO reviewDTO) {
    // Check if user has already reviewed this booking
    if (hasUserReviewedBooking(reviewDTO.getBookingId(), reviewDTO.getUserId())) {
      throw new RuntimeException("User has already reviewed this booking");
    }

    // Get booking and user
    Booking booking = bookingRepository.findById(reviewDTO.getBookingId())
        .orElseThrow(() -> new RuntimeException("Booking not found"));
    User user = userRepository.findById(reviewDTO.getUserId())
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Create review
    Review review = new Review();
    review.setBooking(booking);
    review.setUser(user);
    review.setRating(reviewDTO.getRating());
    review.setComment(reviewDTO.getComment());

    Review savedReview = reviewRepository.save(review);
    return convertToDTO(savedReview);
  }

  @Override
  public ReviewDTO getReviewById(Long id) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    return convertToDTO(review);
  }

  @Override
  public List<ReviewDTO> getReviewsByBookingId(Long bookingId) {
    return reviewRepository.findByBookingId(bookingId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReviewDTO> getReviewsByUserId(Long userId) {
    return reviewRepository.findByUserId(userId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReviewDTO> getReviewsByRoomId(Long roomId) {
    return reviewRepository.findByRoomId(roomId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  @Override
  public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO) {
    Review review = reviewRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Review not found"));

    review.setRating(reviewDTO.getRating());
    review.setComment(reviewDTO.getComment());

    Review updatedReview = reviewRepository.save(review);
    return convertToDTO(updatedReview);
  }

  @Override
  public void deleteReview(Long id) {
    if (!reviewRepository.existsById(id)) {
      throw new RuntimeException("Review not found");
    }
    reviewRepository.deleteById(id);
  }

  @Override
  public boolean hasUserReviewedBooking(Long bookingId, Long userId) {
    return reviewRepository.findByBookingId(bookingId).stream()
        .anyMatch(review -> review.getUser().getId().equals(userId));
  }

  private ReviewDTO convertToDTO(Review review) {
    ReviewDTO dto = new ReviewDTO();
    dto.setId(review.getId());
    dto.setBookingId(review.getBooking().getId());
    dto.setUserId(review.getUser().getId());
    dto.setRating(review.getRating());
    dto.setComment(review.getComment());
    dto.setCreatedAt(review.getCreatedAt());

    // Set additional display fields
    dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
    dto.setRoomType(review.getBooking().getRoom().getRoomType());

    return dto;
  }
}