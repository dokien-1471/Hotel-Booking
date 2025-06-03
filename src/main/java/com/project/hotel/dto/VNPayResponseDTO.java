package com.project.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayResponseDTO {
    private String transactionId;
    private String responseCode;
    private String orderInfo;
    private Map<String, String> responseData;
    private boolean success;
    private String message;
    private String paymentStatus;
    private Long bookingId;
    private Long paymentId;
}
