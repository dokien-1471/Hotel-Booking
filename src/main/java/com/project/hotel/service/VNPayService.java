package com.project.hotel.service;

import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.dto.VNPayResponseDTO;

public interface VNPayService {
  String createPaymentUrl(VNPayRequestDTO request);

  boolean validatePaymentResponse(String responseCode, String txnRef, String transactionNo);

  VNPayResponseDTO processPaymentResponse(String responseCode, String txnRef, String transactionNo);
}