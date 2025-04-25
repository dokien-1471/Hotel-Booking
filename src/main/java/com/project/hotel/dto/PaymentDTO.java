package com.project.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDateTime paymentTime;
    private String method;
    private String status;
    private Long bookingId;
    private String bookingReference;
    private String transactionId;
    private String paymentReference;
    
    // Additional fields for display purposes
    private String userFullName;
    private String roomNumber;
}
