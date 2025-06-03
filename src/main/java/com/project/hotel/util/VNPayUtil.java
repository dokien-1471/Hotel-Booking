package com.project.hotel.util;

import com.project.hotel.config.VNPayConfig;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VNPayUtil {
  private final VNPayConfig vnPayConfig;

  public VNPayUtil(VNPayConfig vnPayConfig) {
    this.vnPayConfig = vnPayConfig;
  }

  public String createPaymentUrl(String orderId, long amount, String orderInfo) {
    try {
      String vnp_Version = vnPayConfig.getVersion();
      String vnp_Command = vnPayConfig.getCommand();
      String vnp_TxnRef = orderId;
      String vnp_IpAddr = "127.0.0.1";
      String vnp_TmnCode = vnPayConfig.getTmnCode();
      String vnp_HashSecret = vnPayConfig.getHashSecret();
      String vnp_Url = vnPayConfig.getUrl();
      String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
      String vnp_Locale = vnPayConfig.getLocale();
      String vnp_CurrencyCode = vnPayConfig.getCurrencyCode();
      String vnp_OrderType = vnPayConfig.getOrderType();

      Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
      String vnp_CreateDate = formatter.format(cld.getTime());

      cld.add(Calendar.MINUTE, 15);
      String vnp_ExpireDate = formatter.format(cld.getTime());

      Map<String, String> vnp_Params = new HashMap<>();
      vnp_Params.put("vnp_Version", vnp_Version);
      vnp_Params.put("vnp_Command", vnp_Command);
      vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
      vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
      vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
      vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
      vnp_Params.put("vnp_OrderInfo", orderInfo);
      vnp_Params.put("vnp_OrderType", vnp_OrderType);
      vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
      vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
      vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
      vnp_Params.put("vnp_Locale", vnp_Locale);
      vnp_Params.put("vnp_CurrencyCode", vnp_CurrencyCode);

      List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
      Collections.sort(fieldNames);

      StringBuilder hashData = new StringBuilder();
      StringBuilder query = new StringBuilder();
      Iterator<String> itr = fieldNames.iterator();
      while (itr.hasNext()) {
        String fieldName = itr.next();
        String fieldValue = vnp_Params.get(fieldName);
        if ((fieldValue != null) && (fieldValue.length() > 0)) {
          hashData.append(fieldName);
          hashData.append('=');
          hashData.append(fieldValue);
          query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
          query.append('=');
          query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
          if (itr.hasNext()) {
            query.append('&');
            hashData.append('&');
          }
        }
      }
      String queryUrl = query.toString();
      String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
      queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
      return vnp_Url + "?" + queryUrl;
    } catch (Exception e) {
      throw new RuntimeException("Error creating VNPay payment URL", e);
    }
  }

  private String hmacSHA512(String key, String data) {
    try {
      Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      sha512_HMAC.init(secret_key);
      byte[] bytes = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(bytes);
    } catch (Exception e) {
      throw new RuntimeException("Error generating HMAC SHA512", e);
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  public boolean validatePaymentResponse(Map<String, String> responseParams) {
    String vnp_SecureHash = responseParams.get("vnp_SecureHash");
    if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
      return false;
    }
    responseParams.remove("vnp_SecureHash");

    List<String> fieldNames = new ArrayList<>(responseParams.keySet());
    Collections.sort(fieldNames);

    StringBuilder hashData = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = responseParams.get(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        hashData.append(fieldName);
        hashData.append('=');
        hashData.append(fieldValue);
        if (itr.hasNext()) {
          hashData.append('&');
        }
      }
    }

    String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
    return calculatedHash.equals(vnp_SecureHash);
  }
}