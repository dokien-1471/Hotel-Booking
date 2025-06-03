package com.project.hotel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayRequestDTO {
    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private long amount;

    @NotNull(message = "Order info is required")
    private String orderInfo;

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String bankCode;
    private String ipAddress;
}
