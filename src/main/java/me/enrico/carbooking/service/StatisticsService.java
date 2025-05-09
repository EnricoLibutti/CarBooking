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
                .filter(booking -> booking.getCar() != null) // Aggiungi controllo per car nullo
                .collect(Collectors.groupingBy(
                        booking -> booking.getCar().getName(),
                        Collectors.counting()
                ));

        Map<String, Double> hoursPerCar = allBookings.stream()
                .filter(booking -> booking.getCar() != null) // Aggiungi controllo per car nullo
                .collect(Collectors.groupingBy(
                        booking -> booking.getCar().getName(),
                        Collectors.summingDouble(Booking::getDuration)
                ));

        // Statistiche per utente
        // Modificato per usare user.getUsername() o un altro identificatore dell'utente
        Map<String, Long> bookingsPerUser = allBookings.stream()
                .filter(booking -> booking.getUser() != null && booking.getUser().getUsername() != null) // Aggiungi controllo per user e username nullo
                .collect(Collectors.groupingBy(
                        booking -> booking.getUser().getUsername(), // Modificato
                        Collectors.counting()
                ));

        Map<String, Double> hoursPerUser = allBookings.stream()
                .filter(booking -> booking.getUser() != null && booking.getUser().getUsername() != null) // Aggiungi controllo
                .collect(Collectors.groupingBy(
                        booking -> booking.getUser().getUsername(), // Modificato
                        Collectors.summingDouble(Booking::getDuration)
                ));

        Map<String, String> mostUsedCarPerUser = allBookings.stream()
                .filter(booking -> booking.getUser() != null && booking.getUser().getUsername() != null && booking.getCar() != null) // Aggiungi controlli
                .collect(Collectors.groupingBy(
                        booking -> booking.getUser().getUsername(), // Modificato
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
                .filter(booking -> booking.getReason() != null) // Aggiungi controllo per reason nullo
                .collect(Collectors.groupingBy(
                        Booking::getReason,
                        Collectors.counting()
                ));

        // Prenotazioni mensili
        Map<String, Long> monthlyBookings = allBookings.stream()
                .filter(booking -> booking.getStartDateTime() != null) // Aggiungi controllo per startDateTime nullo
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
