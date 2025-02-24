package me.enrico.carbooking.repositories;

import me.enrico.carbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.car WHERE b.active = true AND b.startDateTime <= :now AND b.endDateTime > :now")
    List<Booking> findCurrentlyOccupiedWithCars(@Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b JOIN FETCH b.car WHERE b.active = true AND b.startDateTime > :now")
    List<Booking> findFutureBookingsWithCars(@Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId AND b.active = true")
    List<Booking> findByCarIdAndActiveTrue(@Param("carId") Long carId);
}