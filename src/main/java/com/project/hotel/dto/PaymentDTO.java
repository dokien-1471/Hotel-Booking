package com.project.hotel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "Vui lòng nhập số tiền thanh toán")
    @Positive(message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Vui lòng chọn đơn đặt phòng")
    private Long bookingId;

    private String transactionId; 
    private String status; 
    private LocalDateTime paymentTime; 
    private String method; 
}
