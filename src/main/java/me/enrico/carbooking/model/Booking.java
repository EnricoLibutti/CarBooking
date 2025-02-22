package me.enrico.carbooking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne
    private Car car;

    private String bookedByName;
    private LocalDateTime bookedAt;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int duration;
    private String reason;
    private boolean active = true;

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return !(end.isBefore(startDateTime) || start.isAfter(endDateTime));
    }

    // Add a method to get car details without triggering the circular reference
    @Transient
    public CarInfo getCarInfo() {
        return car != null ? new CarInfo(car.getId(), car.getName(), car.getSeats()) : null;
    }
}