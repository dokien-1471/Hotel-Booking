package com.project.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String roomNumber;
    
    @Column(nullable = false)
    private String roomType; // SINGLE, DOUBLE, SUITE, etc.
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private boolean isAvailable = true;
    
    @Column(length = 1000)
    private String description;
    
    private String photo; // URL or path to the room photo
}
