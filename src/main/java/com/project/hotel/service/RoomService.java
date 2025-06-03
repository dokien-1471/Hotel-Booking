package com.project.hotel.service;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Room;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(RoomDTO roomDTO);

    RoomDTO getRoomById(Long id);

    List<RoomDTO> getAllRooms();

    List<RoomDTO> getAvailableRooms();

    List<RoomDTO> getRoomsByType(String roomType);

    List<RoomDTO> getRoomsBySelectedAmenities(List<String> amenities);

    RoomDTO updateRoom(Long id, RoomDTO roomDTO);

    void deleteRoom(Long id);

    Room findRoomEntityById(Long id);

    // New methods for image management
    RoomDTO addRoomImage(Long roomId, MultipartFile file);

    RoomDTO removeRoomImage(Long roomId, String imageUrl);

    List<String> getRoomImages(Long roomId);
}