package com.project.hotel.service;

import com.project.hotel.util.VNPayUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VNPayService {
  private final VNPayUtil vnPayUtil;

  public VNPayService(VNPayUtil vnPayUtil) {
    this.vnPayUtil = vnPayUtil;
  }

  public String createPaymentUrl(String orderId, long amount, String orderInfo) {
    return vnPayUtil.createPaymentUrl(orderId, amount, orderInfo);
  }

  public boolean validatePaymentResponse(Map<String, String> responseParams) {
    return vnPayUtil.validatePaymentResponse(responseParams);
  }
}