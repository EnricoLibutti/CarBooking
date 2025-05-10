package me.enrico.carbooking.controllers;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.dto.BookingDTO;
import me.enrico.carbooking.dto.CarDTO;
import me.enrico.carbooking.exception.ResourceNotFoundException;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.model.Role;
import me.enrico.carbooking.model.User;
import me.enrico.carbooking.repositories.BookingRepository;
import me.enrico.carbooking.repositories.CarRepository;
import me.enrico.carbooking.request.CarBookingRequest;
import me.enrico.carbooking.service.BookingService;
import me.enrico.carbooking.service.EmailService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.enrico.carbooking.repositories.UserRepository; // Aggiungi questo import
import org.springframework.web.bind.annotation.GetMapping; // Assicurati che GetMapping sia importato
import java.util.Map; // Aggiungi questo import
import java.util.HashMap; // Aggiungi questo import

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final UserRepository userRepository; // Aggiungi questo campo
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");
    private static final Logger logger = LoggerFactory.getLogger(CarController.class);

    @Cacheable("cars")
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> cars = carRepository.findAll();
        return ResponseEntity.ok(cars.stream()
                .map(this::convertToCarDTO)
                .collect(Collectors.toList()));
    }

    @Cacheable("occupiedCars")
    @GetMapping("/occupied")
    public ResponseEntity<List<BookingDTO>> getCurrentlyOccupiedCars() {
        LocalDateTime now = LocalDateTime.now(ROME_ZONE);
        List<Booking> occupiedCars = bookingRepository.findCurrentlyOccupiedWithCars(now);
        return ResponseEntity.ok(occupiedCars.stream()
                .map(this::convertToBookingDTO)
                .collect(Collectors.toList()));
    }

    @Cacheable("futureBookings")
    @GetMapping("/future-bookings")
    public ResponseEntity<List<BookingDTO>> getFutureBookedCars() {
        LocalDateTime now = LocalDateTime.now(ROME_ZONE);
        List<Booking> futureBookings = bookingRepository.findFutureBookingsWithCars(now);
        return ResponseEntity.ok(futureBookings.stream()
                .map(this::convertToBookingDTO)
                .collect(Collectors.toList()));
    }

    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @PostMapping("/book/{id}")
    public ResponseEntity<?> bookCar( // ResponseEntity<?> per una maggiore flessibilità nel corpo della risposta
            @PathVariable Long id,
            @RequestBody CarBookingRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato. Effettua il login per prenotare.");
            }

            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Auto non trovata con id: " + id));

            // ERRORE QUI: createBooking restituisce String, ma si aspetta Booking
            Booking booking = bookingService.createBooking(car, request, currentUser);
            // La conferma email viene ora inviata dal BookingService (questo commento diventerà vero dopo la modifica a BookingService)
            return ResponseEntity.ok(convertToBookingDTO(booking)); // Restituisce il DTO della prenotazione creata
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Tentativo di prenotazione fallito: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            logger.warn("Risorsa non trovata durante la prenotazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la prenotazione dell'auto con id {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore interno del server durante la prenotazione. Riprova più tardi.");
        }
    }

    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @PostMapping("/terminate/{id}")
    public ResponseEntity<String> terminateBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return handleBookingStatusChange(id, "terminata", currentUser, "La tua prenotazione è stata terminata.");
    }

    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return handleBookingStatusChange(id, "annullata", currentUser, "La tua prenotazione è stata annullata.");
    }

    // NUOVO METODO PER INVIARE EMAIL DI TEST
    @GetMapping("/admin/send-test-email")
    public ResponseEntity<String> sendTestEmail(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            logger.warn("Tentativo di invio email di test da utente non autenticato.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.name().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin) {
            logger.warn("Tentativo di invio email di test da utente non admin: {}", currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accesso negato. Solo gli amministratori possono inviare email di test.");
        }

        if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            logger.error("L'utente admin {} non ha un indirizzo email configurato.", currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("L'utente admin non ha un indirizzo email configurato.");
        }

        try {
            Map<String, Object> model = new HashMap<>();
            model.put("userName", currentUser.getFirstName() != null ? currentUser.getFirstName() : currentUser.getUsername());
            // Puoi aggiungere altri dati al modello se necessario per il template

            emailService.sendHtmlEmail(
                    currentUser.getEmail(),
                    "CarBooking - Email di Test",
                    "test-email.html", // Nome del template che creeremo
                    model
            );
            logger.info("Email di test inviata con successo a: {}", currentUser.getEmail());
            return ResponseEntity.ok("Email di test inviata con successo a " + currentUser.getEmail());
        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email di test a {}: {}", currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante l'invio dell'email di test: " + e.getMessage());
        }
    }

    private ResponseEntity<String> handleBookingStatusChange(Long bookingId, String action, User currentUser, String emailReason) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata con id: " + bookingId));

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
            }

            boolean isAdmin = currentUser.getRoles().stream()
                                .anyMatch(role -> role.name().equals(Role.ROLE_ADMIN.name())); // Changed getName() to name() and compared with Role.ROLE_ADMIN.name() for robustness
            boolean isOwner = booking.getUser() != null && booking.getUser().getId().equals(currentUser.getId());

            if (!isOwner && !isAdmin) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non sei autorizzato a modificare questa prenotazione.");
            }

            if (!booking.isActive()) {
                // Se l'azione è "terminata" e la prenotazione è già terminata, o "annullata" e già annullata.
                return ResponseEntity.badRequest().body("La prenotazione è già stata " + action + ".");
            }

            booking.setActive(false);
            // booking.setModifiedBy(currentUser.getUsername()); // Opzionale: tracciare chi ha modificato
            // booking.setModifiedAt(LocalDateTime.now(ROME_ZONE)); // Opzionale: tracciare quando
            bookingRepository.save(booking);

            // Invia email di notifica
            try {
                // Passiamo l'oggetto User completo per avere l'email
                if (booking.getUser() != null && booking.getUser().getEmail() != null) { // Modificato da getUsername() a getEmail()
                    // CORREZIONE: Chiamata al metodo corretto e con i parametri giusti
                    emailService.sendBookingStatusChangeEmail(booking, action, emailReason);
                } else {
                    logger.warn("Impossibile inviare email di notifica per la prenotazione ID {}: utente o email dell'utente non disponibile.", booking.getId());
                }
            } catch (Exception e) {
                 logger.error("ATTENZIONE: La prenotazione ID {} è stata {}, ma l'email di notifica non è stata inviata. Errore: {}", booking.getId(), action, e.getMessage(), e);
            }

            return ResponseEntity.ok("Prenotazione " + action + " con successo!");
        } catch (ResourceNotFoundException e) {
            logger.warn("Risorsa non trovata durante la modifica dello stato della prenotazione (ID: {}): {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la modifica dello stato della prenotazione (ID: {}): ", bookingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore interno del server durante l'operazione. Riprova più tardi.");
        }
    }

    private CarDTO convertToCarDTO(Car car) {
        return CarDTO.builder()
                .id(car.getId())
                .name(car.getName())
                .seats(car.getSeats())
                .available(car.isAvailable()) // Questo potrebbe non essere sempre aggiornato qui, dipende dalla logica di `isAvailable`
                .build();
    }

    private BookingDTO convertToBookingDTO(Booking booking) {
        String bookedByUsername = (booking.getUser() != null) ? booking.getUser().getUsername() : "N/A";
        CarDTO carDto = null;
        if (booking.getCar() != null) {
            carDto = convertToCarDTO(booking.getCar()); // Use the existing method to convert Car to CarDTO
        }

        return BookingDTO.builder()
                .id(booking.getId())
                .car(carDto) // Set the CarDTO object
                .bookedByUsername(bookedByUsername)
                .bookedAt(booking.getBookedAt())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .duration(booking.getDuration())
                .reason(booking.getReason())
                .active(booking.isActive())
                .build();
    }
}