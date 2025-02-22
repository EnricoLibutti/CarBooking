package me.enrico.carbooking.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CarInfo {
    private Long id;
    private String name;
    private int seats;
}