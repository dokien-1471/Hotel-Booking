package com.project.hotel.controller;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.service.RoomService;
import com.project.hotel.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO createdRoom = roomService.createRoom(roomDTO);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        RoomDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms() {
        List<RoomDTO> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/type/{roomType}")
    public ResponseEntity<List<RoomDTO>> getRoomsByType(@PathVariable String roomType) {
        List<RoomDTO> rooms = roomService.getRoomsByType(roomType);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/amenities")
    public ResponseEntity<List<RoomDTO>> getRoomsByAmenities(@RequestParam List<String> amenities) {
        List<RoomDTO> rooms = roomService.getRoomsBySelectedAmenities(amenities);
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDTO roomDTO) {
        RoomDTO updatedRoom = roomService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoomAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        RoomDTO room = roomService.getRoomById(id);
        room.setAvailable(available);
        RoomDTO updatedRoom = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updatedRoom);
    }

    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> updateRoomPrice(
            @PathVariable Long id,
            @RequestParam double price) {
        RoomDTO room = roomService.getRoomById(id);
        room.setPrice(java.math.BigDecimal.valueOf(price));
        RoomDTO updatedRoom = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updatedRoom);
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> addRoomImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        RoomDTO updatedRoom = roomService.addRoomImage(id, file);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomDTO> removeRoomImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {
        RoomDTO updatedRoom = roomService.removeRoomImage(id, imageUrl);
        return ResponseEntity.ok(updatedRoom);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getRoomImages(@PathVariable Long id) {
        List<String> images = roomService.getRoomImages(id);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Boolean>> checkRoomAvailability(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        boolean isAvailable = bookingService.isRoomAvailable(
                id,
                LocalDate.parse(checkIn),
                LocalDate.parse(checkOut));
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
}