package me.enrico.carbooking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatisticsDTO {
    private Map<String, Long> bookingsPerCar;
    private Map<String, Double> hoursPerCar;
    private Map<String, Long> bookingsPerUser;
    private Map<String, Long> reasonDistribution;
    private Map<String, Long> monthlyBookings;
    private double avgDuration;
    private Long totalBookings;
}