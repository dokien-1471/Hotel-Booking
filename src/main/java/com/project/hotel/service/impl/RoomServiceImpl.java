package com.project.hotel.service.impl;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Room;
import com.project.hotel.repository.RoomRepository;
import com.project.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomDTO createRoom(RoomDTO roomDTO) {
        // Check if room with the same room number already exists
        Room existingRoom = roomRepository.findByRoomNumber(roomDTO.getRoomNumber());
        if (existingRoom != null) {
            throw new RuntimeException("Room with number " + roomDTO.getRoomNumber() + " already exists");
        }

        // Convert DTO to entity
        Room room = new Room();
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());
        room.setPhoto(roomDTO.getPhoto());

        // Save room
        Room savedRoom = roomRepository.save(room);

        // Convert saved entity to DTO
        return convertToDTO(savedRoom);
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return convertToDTO(room);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getAvailableRooms() {
        return roomRepository.findByIsAvailable(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getRoomsByType(String roomType) {
        return roomRepository.findByRoomType(roomType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        // Check if room number is being changed and if it's already taken
        if (!room.getRoomNumber().equals(roomDTO.getRoomNumber())) {
            Room existingRoom = roomRepository.findByRoomNumber(roomDTO.getRoomNumber());
            if (existingRoom != null) {
                throw new RuntimeException("Room with number " + roomDTO.getRoomNumber() + " already exists");
            }
        }

        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());
        
        // Update photo only if provided
        if (roomDTO.getPhoto() != null && !roomDTO.getPhoto().isEmpty()) {
            room.setPhoto(roomDTO.getPhoto());
        }

        Room updatedRoom = roomRepository.save(room);
        return convertToDTO(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public Room findRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    private RoomDTO convertToDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomNumber(room.getRoomNumber());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setPrice(room.getPrice());
        roomDTO.setAvailable(room.isAvailable());
        roomDTO.setDescription(room.getDescription());
        roomDTO.setPhoto(room.getPhoto());
        return roomDTO;
    }
}
