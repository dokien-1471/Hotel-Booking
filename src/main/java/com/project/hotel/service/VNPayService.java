package com.project.hotel.service;

import com.project.hotel.config.VNPayConfig;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.constant.PaymentStatus;
import com.project.hotel.constant.BookingStatus;
import com.project.hotel.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.dto.VNPayResponseDTO;

@Service
public interface VNPayService {


  String createPaymentUrl(VNPayRequestDTO request);


  String createPaymentUrl(Long bookingId, HttpServletRequest request);


  boolean validatePaymentResponse(String responseCode, String txnRef, String transactionNo);


  @Transactional
  VNPayResponseDTO processPaymentResponse(String responseCode, String txnRef, String transactionNo);


  @Transactional
  Payment processPaymentReturn(Map<String, String> vnpParams);


  boolean verifySecureHash(Map<String, String> vnpParams, String vnpSecureHash);
}