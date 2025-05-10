package me.enrico.carbooking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.model.User; // Aggiungi import
import me.enrico.carbooking.repositories.BookingRepository;
import me.enrico.carbooking.request.CarBookingRequest;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId; // Assicurati che sia importato
import java.time.ZonedDateTime;
import java.util.List;
import java.time.temporal.ChronoUnit; // Aggiungi import

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService; // Assicurati che sia iniettato
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");
    // Aggiungi un logger se vuoi loggare errori di invio email da questo service
    // private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public Booking createBooking(Car car, CarBookingRequest request, User currentUser) { // Modificato tipo di ritorno da String a Booking
        validateBookingRequest(request);
        checkForOverlappingBookings(car.getId(), request);

        Booking booking = new Booking();
        booking.setCar(car);
        booking.setUser(currentUser);
        booking.setBookedAt(LocalDateTime.now(ROME_ZONE));
        booking.setStartDateTime(request.getStartDateTime());
        booking.setEndDateTime(request.getEndDateTime());
        booking.setDuration(calculateDuration(request));
        booking.setReason(request.getReason());
        booking.setActive(true);

        Booking savedBooking = bookingRepository.save(booking);

        // Invia email di conferma
        try {
            // EmailService.sendBookingConfirmationEmail gestisce già i log per utente/email nulli
            emailService.sendBookingConfirmationEmail(savedBooking);
        } catch (Exception e) {
            // Logga l'errore se l'invio dell'email fallisce, ma non interrompere il processo di prenotazione
         //   logger.error("ATTENZIONE: Prenotazione ID {} creata, ma email di conferma non inviata. Errore: {}", savedBooking.getId(), e.getMessage(), e);
            // L'EmailService dovrebbe già loggare i suoi errori interni.
        }

        return savedBooking; // Restituisce l'oggetto Booking salvato
    }

    private void validateBookingRequest(CarBookingRequest request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            throw new IllegalArgumentException("Le date di inizio e fine prenotazione sono obbligatorie.");
        }
        if (request.getStartDateTime().isBefore(LocalDateTime.now(ROME_ZONE).minusMinutes(5))) { // Tolleranza di 5 minuti per richieste immediate
            throw new IllegalArgumentException("La data di inizio non può essere nel passato.");
        }
        if (request.getEndDateTime().isBefore(request.getStartDateTime()) || request.getEndDateTime().isEqual(request.getStartDateTime())) {
            throw new IllegalArgumentException("La data di fine deve essere successiva alla data di inizio.");
        }
    }

    private void checkForOverlappingBookings(Long carId, CarBookingRequest request) {
        List<Booking> existingBookings = bookingRepository.findByCarIdAndActiveTrue(carId);
        for (Booking existingBooking : existingBookings) {
            if (existingBooking.overlaps(request.getStartDateTime(), request.getEndDateTime())) {
                throw new IllegalStateException("L'auto è già prenotata per il periodo selezionato.");
            }
        }
    }

    private int calculateDuration(CarBookingRequest request) {
        if (request.getStartDateTime() == null || request.getEndDateTime() == null) {
            return 0;
        }
        // Calcola la durata in ore, arrotondando per eccesso
        long minutes = ChronoUnit.MINUTES.between(request.getStartDateTime(), request.getEndDateTime());
        return (int) Math.ceil((double) minutes / 60.0);
    }
}