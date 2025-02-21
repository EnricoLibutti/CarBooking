package me.enrico.carbooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CarBookingDTO {
    private Long id;
    private String name;        // nome dell'auto
    private String bookedByName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer bookedDuration;
    private String reason;
}