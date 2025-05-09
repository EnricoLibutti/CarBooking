package me.enrico.carbooking.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDTO {
    private Long id;
    private CarDTO car;
    // private String bookedByName; // Rimosso
    private String bookedByUsername; // Aggiunto
    private LocalDateTime bookedAt;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int duration;
    private String reason;
    private boolean active;
}