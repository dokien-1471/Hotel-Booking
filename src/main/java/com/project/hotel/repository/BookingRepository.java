package com.project.hotel.repository;

import com.project.hotel.constant.BookingStatus;
import com.project.hotel.entity.Booking;
import com.project.hotel.entity.Room;
import com.project.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    List<Booking> findByRoom(Room room);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate <= :endDate AND b.checkOutDate >= :startDate")
    List<Booking> findBookingsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCheckInDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND ((b.checkInDate BETWEEN :checkIn AND :checkOut) " +
            "OR (b.checkOutDate BETWEEN :checkIn AND :checkOut) " +
            "OR (:checkIn BETWEEN b.checkInDate AND b.checkOutDate))")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    List<Booking> findByRoomAndStatusNot(Room room, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.checkInDate BETWEEN :startDate AND :endDate) " +
            "OR (b.checkOutDate BETWEEN :startDate AND :endDate)")
    List<Booking> findBookingsWithCheckInOrCheckOutInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}