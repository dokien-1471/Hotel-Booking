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

    @NotNull(message = "Vui lòng nhập số phòng")
    private String roomNumber; // Số phòng

    @NotNull(message = "Vui lòng chọn loại phòng")
    private String roomType; // Loại phòng

    @NotNull(message = "Vui lòng nhập giá phòng")
    @Positive(message = "Giá phòng phải lớn hơn 0")
    private BigDecimal price;

    private boolean available;

    private String description;

    private List<String> images;

    private List<String> amenities;

    private String name;

    private String type;
}