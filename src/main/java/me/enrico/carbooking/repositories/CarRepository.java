package me.enrico.carbooking.repositories;

import me.enrico.carbooking.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Query("SELECT DISTINCT c FROM Car c LEFT JOIN FETCH c.bookings b WHERE b.active = true OR b IS NULL")
    List<Car> findAllWithActiveBookings();
}
