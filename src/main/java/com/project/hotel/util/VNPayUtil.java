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
      String vnp_Version = "2.1.0";
      String vnp_Command = "pay";
      String vnp_TxnRef = orderId;
      String vnp_IpAddr = "127.0.0.1";
      String vnp_TmnCode = vnPayConfig.getTmnCode();
      String vnp_HashSecret = vnPayConfig.getHashSecret();
      String vnp_Url = vnPayConfig.getVnpUrl();
      String vnp_ReturnUrl = vnPayConfig.getReturnUrl();

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
      vnp_Params.put("vnp_OrderType", "other");
      vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
      vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
      vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);

      List fieldNames = new ArrayList(vnp_Params.keySet());
      Collections.sort(fieldNames);

      StringBuilder hashData = new StringBuilder();
      StringBuilder query = new StringBuilder();
      Iterator itr = fieldNames.iterator();
      while (itr.hasNext()) {
        String fieldName = (String) itr.next();
        String fieldValue = (String) vnp_Params.get(fieldName);
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
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA512");
      sha512_HMAC.init(secret_key);
      byte[] bytes = sha512_HMAC.doFinal(data.getBytes());
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
    responseParams.remove("vnp_SecureHash");

    List fieldNames = new ArrayList(responseParams.keySet());
    Collections.sort(fieldNames);

    StringBuilder hashData = new StringBuilder();
    Iterator itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = (String) itr.next();
      String fieldValue = (String) responseParams.get(fieldName);
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