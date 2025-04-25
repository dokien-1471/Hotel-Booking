package com.project.hotel.util;

import com.project.hotel.config.VNPayConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class VNPayUtilTest {

  @Mock
  private VNPayConfig vnPayConfig;

  private VNPayUtil vnPayUtil;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(vnPayConfig.getTmnCode()).thenReturn("YOUR_TMN_CODE");
    when(vnPayConfig.getHashSecret()).thenReturn("YOUR_HASH_SECRET");
    when(vnPayConfig.getVnpUrl()).thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
    when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/api/payment/return");

    vnPayUtil = new VNPayUtil(vnPayConfig);
  }

  @Test
  void createPaymentUrl_ShouldReturnValidUrl() {
    // Arrange
    String orderId = "ORDER123";
    long amount = 100000;
    String orderInfo = "Hotel Booking Payment";

    // Act
    String paymentUrl = vnPayUtil.createPaymentUrl(orderId, amount, orderInfo);

    // Assert
    assertNotNull(paymentUrl);
    assertTrue(paymentUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
    assertTrue(paymentUrl.contains("vnp_TxnRef=ORDER123"));
    assertTrue(paymentUrl.contains("vnp_Amount=10000000")); // Amount is multiplied by 100
    assertTrue(paymentUrl.contains("vnp_OrderInfo=Hotel+Booking+Payment"));
  }

  @Test
  void validatePaymentResponse_WithValidResponse_ShouldReturnTrue() {
    // Arrange
    Map<String, String> responseParams = new HashMap<>();
    responseParams.put("vnp_TxnRef", "ORDER123");
    responseParams.put("vnp_Amount", "10000000");
    responseParams.put("vnp_OrderInfo", "Hotel Booking Payment");
    responseParams.put("vnp_ResponseCode", "00");
    responseParams.put("vnp_SecureHash", "YOUR_VALID_HASH"); // This would be the actual hash in real scenario

    // Act
    boolean isValid = vnPayUtil.validatePaymentResponse(responseParams);

    // Assert
    assertTrue(isValid);
  }

  @Test
  void validatePaymentResponse_WithInvalidResponse_ShouldReturnFalse() {
    // Arrange
    Map<String, String> responseParams = new HashMap<>();
    responseParams.put("vnp_TxnRef", "ORDER123");
    responseParams.put("vnp_Amount", "10000000");
    responseParams.put("vnp_OrderInfo", "Hotel Booking Payment");
    responseParams.put("vnp_ResponseCode", "00");
    responseParams.put("vnp_SecureHash", "INVALID_HASH");

    // Act
    boolean isValid = vnPayUtil.validatePaymentResponse(responseParams);

    // Assert
    assertFalse(isValid);
  }
}