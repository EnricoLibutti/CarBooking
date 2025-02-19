package me.enrico.carbooking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Nome dell'auto (es. "Fiat Panda")
    private int seats;   // Numero di posti a sedere
    private boolean available = true; // Disponibilità dell'auto
    private LocalDateTime lastUsed; // Ultimo utilizzo dell'auto
    private LocalDateTime availableUntil; // Data/ora fino a cui l'auto non è disponibile

    private String bookedByName; // Nome dell'utente che ha prenotato l'auto
    private LocalDateTime bookedAt; // Data/ora della prenotazione
    private LocalDateTime bookingStart; // Data/ora di inizio prenotazione
    private LocalDateTime bookingEnd; // Data/ora di fine prenotazione
    private int bookedDuration; // Durata della prenotazione in ore
    private String bookedForReason; // Motivazione della prenotazione

    // Costruttore senza argomenti per JPA
    public Car() {}

    // Costruttore con parametri
    public Car(String name, int seats, boolean available) {
        this.name = name;
        this.seats = seats;
        this.available = available;
    }
}