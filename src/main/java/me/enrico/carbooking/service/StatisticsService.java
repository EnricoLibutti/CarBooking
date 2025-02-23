package me.enrico.carbooking.service;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.dto.StatisticsDTO;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookingRepository bookingRepository;

    public StatisticsDTO getStatistics() {
        List<Booking> allBookings = bookingRepository.findAll();

        // Statistiche per auto
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

        // Statistiche per utente
        Map<String, Long> bookingsPerUser = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getBookedByName,
                        Collectors.counting()
                ));

        Map<String, Double> hoursPerUser = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getBookedByName,
                        Collectors.summingDouble(Booking::getDuration)
                ));

        Map<String, String> mostUsedCarPerUser = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getBookedByName,
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(booking -> booking.getCar().getName(), Collectors.counting()),
                                carMap -> carMap.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse("Nessuna auto")
                        )
                ));

        // Distribuzione motivi prenotazione
        Map<String, Long> reasonDistribution = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getReason,
                        Collectors.counting()
                ));

        // Prenotazioni mensili
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
                .hoursPerUser(hoursPerUser)
                .mostUsedCarPerUser(mostUsedCarPerUser)
                .reasonDistribution(reasonDistribution)
                .monthlyBookings(monthlyBookings)
                .avgDuration(avgDuration)
                .totalBookings((long) allBookings.size())
                .build();
    }
}
