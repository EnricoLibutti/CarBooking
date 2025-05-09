package me.enrico.carbooking.controllers;

import lombok.RequiredArgsConstructor;
import me.enrico.carbooking.dto.BookingDTO;
import me.enrico.carbooking.dto.CarDTO;
import me.enrico.carbooking.exception.ResourceNotFoundException;
import me.enrico.carbooking.model.Booking;
import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.model.User; // Import User
import me.enrico.carbooking.repositories.BookingRepository;
import me.enrico.carbooking.repositories.CarRepository;
// import me.enrico.carbooking.repositories.UserRepository; // Option 1: Inject UserRepository
import me.enrico.carbooking.request.CarBookingRequest;
import me.enrico.carbooking.service.BookingService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Option 2: Use @AuthenticationPrincipal
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    // private final UserRepository userRepository; // Option 1
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");


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
        List<Booking> occupiedCars = bookingRepository
                .findCurrentlyOccupiedWithCars(now);
        return ResponseEntity.ok(occupiedCars.stream()
                .map(this::convertToBookingDTO)
                .collect(Collectors.toList()));
    }

    @Cacheable("futureBookings")
    @GetMapping("/future-bookings")
    public ResponseEntity<List<BookingDTO>> getFutureBookedCars() {
        LocalDateTime now = LocalDateTime.now(ROME_ZONE);
        List<Booking> futureBookings = bookingRepository
                .findFutureBookingsWithCars(now);
        return ResponseEntity.ok(futureBookings.stream()
                .map(this::convertToBookingDTO)
                .collect(Collectors.toList()));
    }

    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @PostMapping("/book/{id}")
    public ResponseEntity<String> bookCar(
            @PathVariable Long id,
            @RequestBody CarBookingRequest request,
            @AuthenticationPrincipal User currentUser) { // Option 2: Get authenticated user
        try {
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato.");
            }

            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Auto non trovata con id: " + id));

            // Option 1: Fetch user from DB if not using @AuthenticationPrincipal or if you need a fresh entity
            // User user = userRepository.findByUsername(principal.getName())
            //          .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));

            String result = bookingService.createBooking(car, request, currentUser); // Pass currentUser
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la prenotazione: " + e.getMessage());
        }
    }


    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @PostMapping("/terminate/{id}")
    public ResponseEntity<String> terminateBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) { // Add currentUser for authorization checks
        // TODO: Add logic to check if currentUser is authorized to terminate this booking (e.g., is admin or owner of booking)
        return handleBookingStatusChange(id, "terminata", currentUser);
    }

    @CacheEvict(value = {"cars", "occupiedCars", "futureBookings"}, allEntries = true)
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) { // Add currentUser for authorization checks
        // TODO: Add logic to check if currentUser is authorized to cancel this booking
        return handleBookingStatusChange(id, "annullata", currentUser);
    }

    private ResponseEntity<String> handleBookingStatusChange(Long id, String action, User currentUser) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata con id: " + id));

            // Authorization check: Only admin or the user who made the booking can modify it
            if (currentUser == null || (!booking.getUser().getId().equals(currentUser.getId()) && !currentUser.getRoles().contains(me.enrico.carbooking.model.Role.ROLE_ADMIN))) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorizzato a modificare questa prenotazione.");
            }

            if (!booking.isActive()) {
                return ResponseEntity.badRequest().body("La prenotazione è già stata " + action + "!");
            }

            booking.setActive(false);
            bookingRepository.save(booking);
            return ResponseEntity.ok("Prenotazione " + action + " con successo!");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'operazione: " + e.getMessage());
        }
    }

    private CarDTO convertToCarDTO(Car car) {
        return CarDTO.builder()
                .id(car.getId())
                .name(car.getName())
                .seats(car.getSeats())
                .available(car.isAvailable())
                .build();
    }

    private BookingDTO convertToBookingDTO(Booking booking) {
        // Create a simple UserDTO or just include username if needed in BookingDTO
        // For now, we'll just remove bookedByName
        String bookedByUsername = (booking.getUser() != null) ? booking.getUser().getUsername() : "N/A";

        return BookingDTO.builder()
                .id(booking.getId())
                .car(convertToCarDTO(booking.getCar()))
                // .bookedByName(booking.getBookedByName()) // Rimosso
                .bookedByUsername(bookedByUsername) // Aggiunto per mostrare chi ha prenotato
                .bookedAt(booking.getBookedAt())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .duration(booking.getDuration())
                .reason(booking.getReason())
                .active(booking.isActive())
                .build();
    }
}