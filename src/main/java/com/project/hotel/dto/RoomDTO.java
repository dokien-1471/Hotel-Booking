package com.project.hotel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;

    @NotNull(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private boolean available;

    private String description;

    private List<String> images;

    private List<String> amenities;
}