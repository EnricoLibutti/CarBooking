package me.enrico.carbooking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int seats;
    private boolean available = true;
    private LocalDateTime lastUsed;
    private LocalDateTime availableUntil;

    private String bookedByName;
    private LocalDateTime bookedAt;
    private LocalDateTime bookingStart;
    private LocalDateTime bookingEnd;
    private int bookedDuration;
    private String bookedForReason;


    public Car() {}


    public Car(String name, int seats, boolean available) {
        this.name = name;
        this.seats = seats;
        this.available = available;
    }
}