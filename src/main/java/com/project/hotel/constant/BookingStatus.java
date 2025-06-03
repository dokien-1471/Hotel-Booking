package com.project.hotel.constant;

public enum BookingStatus {
  PENDING, // Đang chờ xác nhận
  CONFIRMED, // Đã xác nhận
  CHECKED_IN, // Đã check-in
  CHECKED_OUT, // Đã check-out
  CANCELLED, // Đã hủy
  PAID // Hoàn thành
}