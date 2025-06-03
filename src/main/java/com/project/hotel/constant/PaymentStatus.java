package com.project.hotel.constant;

public enum PaymentStatus {
  PENDING, // Đang chờ thanh toán
  SUCCESS, // Thanh toán thành công
  FAILED, // Thanh toán thất bại
  REFUNDED, // Đã hoàn tiền
  CANCELLED // Đã hủy
}