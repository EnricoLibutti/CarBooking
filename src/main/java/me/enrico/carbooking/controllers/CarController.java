package me.enrico.carbooking.controllers;

import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.repositories.CarRepository;
import me.enrico.carbooking.request.CarBookingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarRepository carRepository;

    // Ottieni tutte le auto disponibili
    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findByAvailable(true);
    }

    // Prenota un'auto
    @PostMapping("/book/{id}")
    public ResponseEntity<String> bookCar(@PathVariable Long id, @RequestBody CarBookingRequest request) {
        Car car = carRepository.findById(id).orElse(null);
        if (car != null && car.isAvailable()) {
            car.setAvailable(false);
            car.setLastUsed(LocalDateTime.now());
            car.setBookedByName(request.getName());
            car.setBookedAt(LocalDateTime.now());
            car.setBookingStart(car.getLastUsed());
            car.setBookingEnd(car.getLastUsed().plusHours(request.getDuration()));
            car.setBookedDuration(request.getDuration());
            car.setBookedForReason(request.getReason());
            car.setAvailableUntil(car.getBookingEnd());
            carRepository.save(car);
            return ResponseEntity.ok("Car booked successfully for " + request.getDuration() + " hours!");
        }
        return ResponseEntity.badRequest().body("Car not available!");
    }

    // Verifica la disponibilità delle auto
    @GetMapping("/availability")
    public List<Car> checkAvailability() {
        LocalDateTime now = LocalDateTime.now();
        return carRepository.findAll().stream()
                .filter(car -> car.getAvailableUntil() == null || car.getAvailableUntil().isBefore(now))
                .peek(car -> {
                    if (!car.isAvailable()) {
                        car.setAvailable(true);
                        car.setAvailableUntil(null);
                        carRepository.save(car);
                    }
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/terminate/{id}")
    public ResponseEntity<String> terminateBooking(@PathVariable Long id) {
        Car car = carRepository.findById(id).orElse(null);
        if (car != null && !car.isAvailable()) {
            car.setAvailable(true);
            car.setAvailableUntil(null);
            car.setBookedByName(null);
            car.setBookedForReason(null);
            carRepository.save(car);
            return ResponseEntity.ok("Booking terminated successfully!");
        }
        return ResponseEntity.badRequest().body("Car is not currently booked!");
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        Car car = carRepository.findById(id).orElse(null);
        if (car != null && !car.isAvailable()) {
            car.setAvailable(true);
            car.setAvailableUntil(null);
            car.setBookedByName(null);
            car.setBookedForReason(null);
            carRepository.save(car);
            return ResponseEntity.ok("Booking cancelled successfully!");
        }
        return ResponseEntity.badRequest().body("Car is not currently booked!");
    }

    @GetMapping("/calendar")
    public List<Car> getCalendar() {
        LocalDateTime now = LocalDateTime.now();
        return carRepository.findAll().stream()
                .filter(car -> car.getAvailableUntil() != null && car.getAvailableUntil().isAfter(now))
                .collect(Collectors.toList());
    }

    @GetMapping("/occupied")
    public List<Car> getOccupiedCars() {
        LocalDateTime now = LocalDateTime.now();
        return carRepository.findAll().stream()
                .filter(car -> car.getAvailableUntil() != null && car.getAvailableUntil().isAfter(now))
                .collect(Collectors.toList());
    }
}
