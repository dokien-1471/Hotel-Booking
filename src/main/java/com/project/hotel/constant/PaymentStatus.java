package com.project.hotel.constant;

public enum PaymentStatus {
  PENDING, // Đang chờ thanh toán
  PAID, // Thanh toán thành công
  FAILED, // Thanh toán thất bại
  REFUNDED, // Đã hoàn tiền
  EXPIRED, // Hết hạn thanh toán
  CANCELLED // Đã hủy
}