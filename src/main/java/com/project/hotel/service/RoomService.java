package com.project.hotel.service;

import com.project.hotel.dto.RoomDTO;
import com.project.hotel.entity.Room;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(RoomDTO roomDTO);
    RoomDTO getRoomById(Long id);
    List<RoomDTO> getAllRooms();
    List<RoomDTO> getAvailableRooms();
    List<RoomDTO> getRoomsByType(String roomType);
    RoomDTO updateRoom(Long id, RoomDTO roomDTO);
    void deleteRoom(Long id);
    Room findRoomEntityById(Long id);
}