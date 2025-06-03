package com.project.hotel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
  private String tmnCode;
  private String hashSecret;
  private String url;
  private String returnUrl;
  private String command;
  private String orderType;
  private String locale;
  private String currencyCode;
  private String version;

  // Getters and Setters
  public String getTmnCode() {
    return tmnCode;
  }

  public void setTmnCode(String tmnCode) {
    this.tmnCode = tmnCode;
  }

  public String getHashSecret() {
    return hashSecret;
  }

  public void setHashSecret(String hashSecret) {
    this.hashSecret = hashSecret;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}