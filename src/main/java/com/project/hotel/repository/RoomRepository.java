package com.project.hotel.repository;

import com.project.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsAvailable(boolean isAvailable);
    
    @Query("SELECT r FROM Room r WHERE r.roomType = ?1")
    List<Room> findByRoomType(String roomType);
    
    Room findByRoomNumber(String roomNumber);
}
