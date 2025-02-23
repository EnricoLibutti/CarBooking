package me.enrico.carbooking.service;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.dto.StatisticsDTO;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.repositories.BookingRepository;
import me.enrico.carbooking.repositories.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookingRepository bookingRepository;

    public StatisticsDTO getStatistics() {
        List<Booking> allBookings = bookingRepository.findAll();

        // Calcola statistiche
        Map<String, Long> bookingsPerCar = allBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getCar().getName(),
                        Collectors.counting()
                ));

        Map<String, Double> hoursPerCar = allBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getCar().getName(),
                        Collectors.summingDouble(Booking::getDuration)
                ));

        Map<String, Long> bookingsPerUser = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getBookedByName,
                        Collectors.counting()
                ));

        Map<String, Long> reasonDistribution = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getReason,
                        Collectors.counting()
                ));

        Map<String, Long> monthlyBookings = allBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> String.format("%d-%02d",
                                booking.getStartDateTime().getYear(),
                                booking.getStartDateTime().getMonthValue()),
                        Collectors.counting()
                ));

        double avgDuration = allBookings.stream()
                .mapToDouble(Booking::getDuration)
                .average()
                .orElse(0.0);

        return StatisticsDTO.builder()
                .bookingsPerCar(bookingsPerCar)
                .hoursPerCar(hoursPerCar)
                .bookingsPerUser(bookingsPerUser)
                .reasonDistribution(reasonDistribution)
                .monthlyBookings(monthlyBookings)
                .avgDuration(avgDuration)
                .totalBookings((long) allBookings.size())
                .build();
    }
}