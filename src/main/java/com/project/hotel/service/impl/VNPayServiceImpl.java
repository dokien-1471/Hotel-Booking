package com.project.hotel.service.impl;

import com.project.hotel.config.VNPayConfig;
import com.project.hotel.dto.VNPayRequestDTO;
import com.project.hotel.dto.VNPayResponseDTO;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Payment;
import com.project.hotel.exception.PaymentProcessingException;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.repository.BookingRepository;
import com.project.hotel.repository.PaymentRepository;
import com.project.hotel.service.VNPayService;
import com.project.hotel.constant.PaymentStatus;
import com.project.hotel.constant.BookingStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public String createPaymentUrl(VNPayRequestDTO request) {
        try {
            validatePaymentRequest(request);

            Map<String, String> vnpParams = buildPaymentParams(request);
            String queryUrl = buildQueryUrl(vnpParams);
            String secureHash = calculateSecureHash(vnpParams);

            String paymentUrl = vnPayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
            log.info("Created VNPay payment URL for booking {}: {}", request.getBookingId(), paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for booking {}: {}", request.getBookingId(), e.getMessage(), e);
            throw new PaymentProcessingException("Failed to create payment URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String createPaymentUrl(Long bookingId, HttpServletRequest request) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
            validateBooking(booking);

            VNPayRequestDTO vnPayRequest = new VNPayRequestDTO();
            vnPayRequest.setBookingId(booking.getId());
            vnPayRequest.setOrderId("ORDER_" + System.currentTimeMillis());
            vnPayRequest.setAmount(booking.getTotalPrice().longValue());
            vnPayRequest.setOrderInfo("Thanh toan dat phong " + booking.getBookingReference());
            vnPayRequest.setIpAddress(getClientIp(request));

            return createPaymentUrl(vnPayRequest);
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for booking {}: {}", bookingId, e.getMessage(), e);
            throw new PaymentProcessingException("Failed to create payment URL for booking: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validatePaymentResponse(String responseCode, String txnRef, String transactionNo) {
        if (!StringUtils.hasText(responseCode) || !StringUtils.hasText(txnRef) || !StringUtils.hasText(transactionNo)) {
            log.warn("Invalid payment response parameters: responseCode={}, txnRef={}, transactionNo={}",
                    responseCode, txnRef, transactionNo);
            return false;
        }

        try {
            Payment payment = paymentRepository.findByTransactionId(txnRef)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction: " + txnRef));

            boolean isValid = payment != null && "00".equals(responseCode);
            log.info("Payment validation result for transaction {}: {}", txnRef, isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error validating payment response for transaction {}: {}", txnRef, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public VNPayResponseDTO processPaymentResponse(String responseCode, String txnRef, String transactionNo) {
        VNPayResponseDTO responseDTO = new VNPayResponseDTO();
        responseDTO.setResponseCode(responseCode);
        responseDTO.setTransactionId(transactionNo);

        try {
            if (!validatePaymentResponse(responseCode, txnRef, transactionNo)) {
                responseDTO.setSuccess(false);
                responseDTO.setMessage("Invalid payment response");
                log.warn("Invalid payment response for transaction {}: {}", txnRef, responseCode);
                return responseDTO;
            }

            Payment payment = paymentRepository.findByTransactionId(txnRef)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for transaction: " + txnRef));

            updatePaymentStatus(payment, responseCode);
            payment = paymentRepository.save(payment);

            responseDTO.setSuccess(true);
            responseDTO.setMessage("Payment processed successfully");
            responseDTO.setPaymentStatus(payment.getStatus().name());
            responseDTO.setBookingId(payment.getBooking().getId());
            responseDTO.setPaymentId(payment.getId());

            log.info("Successfully processed payment for transaction {}: {}", txnRef, payment.getStatus());

        } catch (Exception e) {
            log.error("Error processing payment response for transaction {}: {}", txnRef, e.getMessage(), e);
            responseDTO.setSuccess(false);
            responseDTO.setMessage("Error processing payment: " + e.getMessage());
        }

        return responseDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment processPaymentReturn(Map<String, String> vnpParams) {
        try {
            // Remove vnp_SecureHash before validating
            String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
            String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
            String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
            String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");

            // Extract bookingId from TxnRef
            String[] txnRefParts = vnp_TxnRef.split("-");
            if (txnRefParts.length != 2) {
                throw new PaymentProcessingException("Invalid transaction reference format");
            }
            Long bookingId = Long.parseLong(txnRefParts[0]);

            // Remove hash from params before validation
            vnpParams.remove("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHashType");

            // Validate hash
            String signValue = hashAllFields(vnpParams);
            if (!signValue.equals(vnp_SecureHash)) {
                throw new PaymentProcessingException("Invalid signature");
            }

            // Get booking with lock
            Booking booking = bookingRepository.findByIdWithLock(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            // Check if payment already processed
            Optional<Payment> existingPayment = paymentRepository.findByTransactionId(vnp_TransactionNo);
            if (existingPayment.isPresent()) {
                log.warn("Payment already processed for transaction {}", vnp_TransactionNo);
                return existingPayment.get();
            }

            // Create payment record
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPrice());
            payment.setTransactionId(vnp_TransactionNo);
            payment.setPaymentTime(LocalDateTime.now());

            // Update status based on response code
            if ("00".equals(vnp_ResponseCode)) {
                booking.setStatus(BookingStatus.CONFIRMED);
                payment.setStatus(PaymentStatus.PAID);
                log.info("Payment successful for booking {}", bookingId);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment failed for booking {} with response code {}", bookingId, vnp_ResponseCode);
            }

            bookingRepository.save(booking);
            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error processing payment return: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to process payment return", e);
        }
    }

    @Override
    public boolean verifySecureHash(Map<String, String> vnpParams, String vnpSecureHash) {
        try {
            if (!StringUtils.hasText(vnpSecureHash)) {
                log.warn("Missing secure hash in payment parameters");
                return false;
            }

            Map<String, String> params = new HashMap<>(vnpParams);
            params.remove("vnp_SecureHash");

            String calculatedHash = calculateSecureHash(params);
            boolean isValid = calculatedHash.equals(vnpSecureHash);

            if (!isValid) {
                log.warn("Invalid secure hash. Expected: {}, Got: {}", calculatedHash, vnpSecureHash);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error verifying secure hash: {}", e.getMessage(), e);
            return false;
        }
    }

    // Private helper methods

    private void validatePaymentRequest(VNPayRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        if (request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (!StringUtils.hasText(request.getOrderInfo())) {
            throw new IllegalArgumentException("Order info is required");
        }
        if (!StringUtils.hasText(request.getIpAddress())) {
            throw new IllegalArgumentException("IP address is required");
        }
        if (!StringUtils.hasText(request.getOrderId())) {
            throw new IllegalArgumentException("Order ID is required");
        }

        // Validate booking exists and is in valid state
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new PaymentProcessingException("Booking is not in pending state");
        }

        // Check if payment already exists
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new PaymentProcessingException("Payment already exists for this booking");
        }
    }

    private void validateBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (booking.getId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (booking.getTotalPrice() == null || booking.getTotalPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid booking amount");
        }
    }

    private Map<String, String> buildPaymentParams(VNPayRequestDTO request) {
        String txnRef = request.getBookingId() + "-" + System.currentTimeMillis();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100)); // Convert to VND (x100)
        vnpParams.put("vnp_CurrCode", vnPayConfig.getCurrencyCode());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", vnPayConfig.getLocale());
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", request.getIpAddress());

        // Add create date
        LocalDateTime now = LocalDateTime.now();
        String createDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpParams.put("vnp_CreateDate", createDate);

        // Add expire date (15 minutes from now)
        LocalDateTime expireDate = now.plusMinutes(15);
        vnpParams.put("vnp_ExpireDate", expireDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Sort params before calculating secure hash
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        // Build hash data
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        String paymentUrl = vnPayConfig.getUrl() + "?" + queryUrl;
        return vnpParams;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            sha512_HMAC.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = sha512_HMAC.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new PaymentProcessingException("Error calculating HMAC-SHA512", e);
        }
    }

    private String buildQueryUrl(Map<String, String> vnpParams) {
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (StringUtils.hasText(fieldValue)) {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }
        return query.toString();
    }

    private String calculateSecureHash(Map<String, String> vnpParams) {
        try {
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (StringUtils.hasText(fieldValue)) {
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName).append('=').append(fieldValue);
                }
            }

            String calculatedHash = calculateHmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            return calculatedHash;
        } catch (Exception e) {
            throw new PaymentProcessingException("Error calculating secure hash", e);
        }
    }

    private void updatePaymentStatus(Payment payment, String responseCode) {
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.PAID);
            payment.getBooking().setStatus(BookingStatus.CONFIRMED);
            log.info("Payment successful for booking {}: {}", payment.getBooking().getId(), payment.getStatus());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.getBooking().setStatus(BookingStatus.CANCELLED);
            log.warn("Payment failed for booking {} with response code: {}", payment.getBooking().getId(),
                    responseCode);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // Try X-Forwarded-For first
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }

        // Try Proxy-Client-IP
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty()) {
            return proxyClientIp;
        }

        // Try WL-Proxy-Client-IP
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty()) {
            return wlProxyClientIp;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    private boolean verifyHash(Map<String, String> params) {
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        String signValue = calculateHmacSHA512(vnPayConfig.getHashSecret(),
                createRawHash(new TreeMap<>(params)));
        return signValue.equals(vnp_SecureHash);
    }

    private String createRawHash(Map<String, String> params) {
        StringBuilder hashData = new StringBuilder();
        params.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append('=').append(value).append('&');
            }
        });
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }
        return hashData.toString();
    }

    private String calculateHmacSHA512(String key, String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hmacData = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacData);
        } catch (Exception e) {
            log.error("Error calculating HMAC-SHA512", e);
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return calculateHmacSHA512(vnPayConfig.getHashSecret(), sb.toString());
    }
}