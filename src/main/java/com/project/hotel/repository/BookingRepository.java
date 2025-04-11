package com.project.hotel.repository;

import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Room;
import com.project.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    
    List<Booking> findByRoom(Room room);
    
    List<Booking> findByStatus(String status);
    
    @Query("SELECT b FROM Booking b WHERE b.checkInDate <= ?2 AND b.checkOutDate >= ?1")
    List<Booking> findBookingsInDateRange(LocalDate startDate, LocalDate endDate);
    
    Booking findByBookingReference(String bookingReference);
}
