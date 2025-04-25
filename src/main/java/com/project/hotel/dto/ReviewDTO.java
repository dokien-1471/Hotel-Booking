package com.project.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
  private Long id;

  @NotNull(message = "Booking ID is required")
  private Long bookingId;

  @NotNull(message = "User ID is required")
  private Long userId;

  @NotNull(message = "Rating is required")
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must be at most 5")
  private Integer rating;

  @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
  private String comment;

  private LocalDateTime createdAt;

  // Additional fields for display
  private String userName;
  private String roomType;
}