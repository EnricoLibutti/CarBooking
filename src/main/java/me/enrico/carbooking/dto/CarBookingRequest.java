package me.enrico.carbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CarBookingRequest {
    private String name;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String reason;
}