package com.project.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDTO {
  private LocalDate date;
  private BigDecimal totalRevenue;
  private Long totalBookings;
  private BigDecimal averageBookingValue;
}