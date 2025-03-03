package me.enrico.carbooking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.repositories.BookingRepository;
import me.enrico.carbooking.request.CarBookingRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        booking.setBookedAt(ZonedDateTime.now(ROME_ZONE).toLocalDateTime());
        booking.setStartDateTime(request.getStartDateTime().atZone(ROME_ZONE).toLocalDateTime());
        booking.setEndDateTime(request.getEndDateTime().atZone(ROME_ZONE).toLocalDateTime());
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

    }

    private void checkForOverlappingBookings(Long carId, CarBookingRequest request) {
        List<Booking> existingBookings = bookingRepository.findByCarIdAndActiveTrue(carId);
        LocalDateTime requestStart = request.getStartDateTime().atZone(ROME_ZONE).toLocalDateTime();
        LocalDateTime requestEnd = request.getEndDateTime().atZone(ROME_ZONE).toLocalDateTime();

        boolean hasOverlap = existingBookings.stream()
                .anyMatch(booking -> booking.overlaps(requestStart, requestEnd));

        if (hasOverlap) {
            throw new IllegalArgumentException("L'auto è già prenotata per il periodo selezionato!");
        }
    }

    private int calculateDuration(CarBookingRequest request) {
        return (int) Duration.between(request.getStartDateTime(), request.getEndDateTime()).toHours();
    }
}