package com.project.hotel.service.impl;

import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.dto.VNPayResponseDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.VNPayService;
import com.project.hotel.util.VNPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VNPayUtil vnPayUtil;

    @Value("${vnpay.tmn-code}")
    private String merchantId;

    @Value("${vnpay.hash-secret}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String paymentUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Autowired
    public VNPayServiceImpl(BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            VNPayUtil vnPayUtil) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.vnPayUtil = vnPayUtil;
    }

    @Override
    @Transactional
    public String createPaymentUrl(VNPayRequestDTO request) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderInfo = request.getOrderInfo();
            String vnp_OrderType = "billpayment";
            String vnp_TxnRef = String.valueOf(request.getBookingId());
            String vnp_IpAddr = request.getIpAddress();
            String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String vnp_CurrCode = "VND";
            String vnp_Locale = "vn";
            long vnp_Amount = request.getAmount() * 100; // Convert to smallest currency unit

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", merchantId);
            vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
            vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
            vnp_Params.put("vnp_BankCode", request.getBankCode());
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", vnp_Locale);
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            // Sort parameters by key
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return paymentUrl + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL", e);
        }
    }

    @Override
    public boolean validatePaymentResponse(String responseCode, String txnRef, String transactionNo) {
        Map<String, String> responseParams = new HashMap<>();
        responseParams.put("vnp_ResponseCode", responseCode);
        responseParams.put("vnp_TxnRef", txnRef);
        responseParams.put("vnp_TransactionNo", transactionNo);
        return vnPayUtil.validatePaymentResponse(responseParams);
    }

    @Override
    @Transactional
    public VNPayResponseDTO processPaymentResponse(String responseCode, String txnRef, String transactionNo) {
        VNPayResponseDTO responseDTO = new VNPayResponseDTO();

        // Create response data map
        Map<String, String> responseData = new HashMap<>();
        responseData.put("vnp_ResponseCode", responseCode);
        responseData.put("vnp_TxnRef", txnRef);
        responseData.put("vnp_TransactionNo", transactionNo);
        responseDTO.setResponseData(responseData);

        // Validate the payment response from VNPay
        boolean isValid = validatePaymentResponse(responseCode, txnRef, transactionNo);
        responseDTO.setSuccess(isValid);

        if (!isValid) {
            responseDTO.setMessage("Invalid payment response");
            return responseDTO;
        }

        // Find the payment by transaction ID
        Payment payment = paymentRepository.findByTransactionId(txnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Process payment status based on response code
        if ("00".equals(responseCode)) {
            // Payment successful
            payment.setStatus("SUCCESS");
            responseDTO.setMessage("Payment successful");
        } else {
            // Payment failed
            payment.setStatus("FAILED");
            responseDTO.setMessage("Payment failed with code: " + responseCode);
        }

        // Update payment in database
        payment = paymentRepository.save(payment);

        // Set response details
        responseDTO.setTransactionId(transactionNo);
        responseDTO.setPaymentStatus(payment.getStatus());
        responseDTO.setBookingId(payment.getBooking().getId());
        responseDTO.setPaymentId(payment.getId());

        return responseDTO;
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC-SHA512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}