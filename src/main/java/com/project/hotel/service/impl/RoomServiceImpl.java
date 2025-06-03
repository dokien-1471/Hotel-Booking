package com.project.hotel.service.impl;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Room;
import com.project.hotel.exception.ResourceNotFoundException;
import com.project.hotel.exception.RoomAlreadyExistsException;
import com.project.hotel.repository.RoomRepository;
import com.project.hotel.service.RoomService;
import com.project.hotel.constant.RoomType;
import com.project.hotel.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private static final String UPLOAD_DIR = "uploads/rooms";

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO roomDTO) {
        // Validate room number uniqueness
        if (roomRepository.findByRoomNumber(roomDTO.getRoomNumber()) != null) {
            throw new RoomAlreadyExistsException("Room with number " + roomDTO.getRoomNumber() + " already exists");
        }

        // Convert DTO to entity
        Room room = convertToEntity(roomDTO);

        // Save room
        Room savedRoom = roomRepository.save(room);

        // Convert saved entity to DTO
        return convertToDTO(savedRoom);
    }

    @Override
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
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
    public List<RoomDTO> getRoomsBySelectedAmenities(List<String> amenities) {
        return roomRepository.findBySelectedAmenities(amenities, amenities.size()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) {
        // Find existing room
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        // Check if room number is being changed and if it's already taken
        if (!room.getRoomNumber().equals(roomDTO.getRoomNumber())) {
            if (roomRepository.findByRoomNumber(roomDTO.getRoomNumber()) != null) {
                throw new RoomAlreadyExistsException("Room with number " + roomDTO.getRoomNumber() + " already exists");
            }
        }

        // Update room properties
        updateRoomFromDTO(room, roomDTO);

        // Save updated room
        Room updatedRoom = roomRepository.save(room);

        // Convert to DTO and return
        return convertToDTO(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }

    @Override
    public Room findRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    @Override
    @Transactional
    public RoomDTO addRoomImage(Long roomId, MultipartFile file) {
        Room room = findRoomEntityById(roomId);

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = FileUploadUtil.saveFile(UPLOAD_DIR, fileName, file);
            String imageUrl = "/api/uploads/" + fileName;

            room.getImages().add(imageUrl);
            Room updatedRoom = roomRepository.save(room);
            return convertToDTO(updatedRoom);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RoomDTO removeRoomImage(Long roomId, String imageUrl) {
        Room room = findRoomEntityById(roomId);

        if (room.getImages().remove(imageUrl)) {
            try {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                FileUploadUtil.deleteFile(UPLOAD_DIR + "/" + fileName);
                Room updatedRoom = roomRepository.save(room);
                return convertToDTO(updatedRoom);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image: " + e.getMessage());
            }
        }
        throw new ResourceNotFoundException("Image not found in room: " + imageUrl);
    }

    @Override
    public List<String> getRoomImages(Long roomId) {
        Room room = findRoomEntityById(roomId);
        return room.getImages();
    }

    // Helper methods for conversion
    private RoomDTO convertToDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setId(room.getId());
        roomDTO.setRoomNumber(room.getRoomNumber());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setPrice(room.getPrice());
        roomDTO.setAvailable(room.isAvailable());
        roomDTO.setDescription(room.getDescription());
        roomDTO.setImages(room.getImages());
        roomDTO.setAmenities(room.getAmenities());
        return roomDTO;
    }

    private Room convertToEntity(RoomDTO roomDTO) {
        Room room = new Room();
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());
        if (roomDTO.getImages() != null) {
            room.setImages(roomDTO.getImages());
        }
        if (roomDTO.getAmenities() != null) {
            room.setAmenities(roomDTO.getAmenities());
        }
        return room;
    }

    private void updateRoomFromDTO(Room room, RoomDTO roomDTO) {
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPrice(roomDTO.getPrice());
        room.setAvailable(roomDTO.isAvailable());
        room.setDescription(roomDTO.getDescription());
        if (roomDTO.getImages() != null) {
            room.setImages(roomDTO.getImages());
        }
        if (roomDTO.getAmenities() != null) {
            room.setAmenities(roomDTO.getAmenities());
        }
    }
}