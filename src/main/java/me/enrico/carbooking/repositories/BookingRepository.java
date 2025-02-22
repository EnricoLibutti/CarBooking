package me.enrico.carbooking.repositories;

import me.enrico.carbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCarIdAndActiveTrue(Long carId);
    List<Booking> findByActiveTrueAndStartDateTimeBeforeAndEndDateTimeAfter(
            LocalDateTime now, LocalDateTime now2);
    List<Booking> findByActiveTrueAndStartDateTimeAfter(LocalDateTime now);
}
