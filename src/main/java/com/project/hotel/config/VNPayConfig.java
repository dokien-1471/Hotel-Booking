package com.project.hotel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration
public class VNPayConfig {
  @Value("${vnpay.tmn-code}")
  private String tmnCode;

  @Value("${vnpay.hash-secret}")
  private String hashSecret;

  @Value("${vnpay.url}")
  private String url;

  @Value("${vnpay.return-url}")
  private String returnUrl;

  @Value("${vnpay.version}")
  private String version = "2.1.0";

  @Value("${vnpay.command}")
  private String command = "pay";

  @Value("${vnpay.order-type}")
  private String orderType = "billpayment";

  @Value("${vnpay.locale}")
  private String locale = "vn";

  @Value("${vnpay.currency-code}")
  private String currencyCode = "VND";

  @Value("${vnpay.timeout}")
  private int timeout = 15; // minutes
}