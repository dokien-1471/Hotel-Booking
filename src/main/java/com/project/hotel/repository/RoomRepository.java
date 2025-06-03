package com.project.hotel.repository;

import com.project.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsAvailable(boolean isAvailable);

    List<Room> findByRoomType(String roomType);

    Room findByRoomNumber(String roomNumber);

    @Query(value = "SELECT DISTINCT r.* FROM rooms r " +
            "JOIN room_amenities ra ON r.id = ra.room_id " +
            "WHERE ra.amenity IN (:amenities) " +
            "GROUP BY r.id " +
            "HAVING COUNT(DISTINCT ra.amenity) = :size", nativeQuery = true)
    List<Room> findBySelectedAmenities(@Param("amenities") List<String> amenities, @Param("size") int size);
}