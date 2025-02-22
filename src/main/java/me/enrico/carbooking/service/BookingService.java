package me.enrico.carbooking.service;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.dto.CarBookingRequest;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.repositories.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");

    @Transactional
    public String createBooking(Car car, CarBookingRequest request) {
        validateBookingRequest(request);
        checkForOverlappingBookings(car.getId(), request);

        Booking booking = new Booking();
        booking.setCar(car);
        booking.setBookedByName(request.getName());
        booking.setBookedAt(LocalDateTime.now(ROME_ZONE));
        booking.setStartDateTime(request.getStartDateTime());
        booking.setEndDateTime(request.getEndDateTime());
        booking.setDuration(calculateDuration(request));
        booking.setReason(request.getReason());
        booking.setActive(true);

        bookingRepository.save(booking);

        return String.format("Hai prenotato con successo l'auto %s!", car.getName());
    }

    private void validateBookingRequest(CarBookingRequest request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            throw new IllegalArgumentException("Le date di inizio e fine sono obbligatorie");
        }

        if (request.getStartDateTime().isAfter(request.getEndDateTime())) {
            throw new IllegalArgumentException("La data di inizio deve essere precedente alla data di fine");
        }

        if (request.getStartDateTime().isBefore(LocalDateTime.now(ROME_ZONE))) {
            throw new IllegalArgumentException("Non puoi prenotare per una data passata");
        }
    }

    private void checkForOverlappingBookings(Long carId, CarBookingRequest request) {
        List<Booking> existingBookings = bookingRepository.findByCarIdAndActiveTrue(carId);
        boolean hasOverlap = existingBookings.stream()
                .anyMatch(booking -> booking.overlaps(request.getStartDateTime(), request.getEndDateTime()));

        if (hasOverlap) {
            throw new IllegalArgumentException("L'auto è già prenotata per il periodo selezionato!");
        }
    }

    private int calculateDuration(CarBookingRequest request) {
        return (int) Duration.between(request.getStartDateTime(), request.getEndDateTime()).toHours();
    }
}