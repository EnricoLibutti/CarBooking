package me.enrico.carbooking.request;

import lombok.Data;

@Data
public class CarBookingRequest {
    private String name;
    private int duration;
    private String reason;
}
