package me.enrico.carbooking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarDTO {
    private Long id;
    private String name;
    private int seats;
    private boolean available;
}