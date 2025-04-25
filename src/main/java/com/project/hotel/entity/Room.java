package com.project.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
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
<<<<<<< HEAD
}
=======
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();
}
>>>>>>> 8eba4e09bc0792bc00a45d21d5208f350a00e30e
