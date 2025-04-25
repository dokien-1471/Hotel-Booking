package com.project.hotel.controller;

import com.project.hotel.dto.ReviewDTO;
import com.project.hotel.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

  private final ReviewService reviewService;

  @Autowired
  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @PostMapping
  public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
    return ResponseEntity.ok(reviewService.createReview(reviewDTO));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
    return ResponseEntity.ok(reviewService.getReviewById(id));
  }

  @GetMapping("/booking/{bookingId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByBookingId(@PathVariable Long bookingId) {
    return ResponseEntity.ok(reviewService.getReviewsByBookingId(bookingId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByRoomId(@PathVariable Long roomId) {
    return ResponseEntity.ok(reviewService.getReviewsByRoomId(roomId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ReviewDTO> updateReview(
      @PathVariable Long id,
      @Valid @RequestBody ReviewDTO reviewDTO) {
    return ResponseEntity.ok(reviewService.updateReview(id, reviewDTO));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
    reviewService.deleteReview(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/check")
  public ResponseEntity<Boolean> hasUserReviewedBooking(
      @RequestParam Long bookingId,
      @RequestParam Long userId) {
    return ResponseEntity.ok(reviewService.hasUserReviewedBooking(bookingId, userId));
  }
}