package me.enrico.carbooking.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString; // Aggiunto per evitare problemi con Lombok e relazioni bidirezionali

import java.time.LocalDateTime;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference // Mantiene la gestione della serializzazione JSON per Car
    @ManyToOne(fetch = FetchType.LAZY) // È buona norma usare LAZY per le relazioni @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY) // Nuova relazione con User
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // Evita cicli con Lombok ToString se User ha una lista di Booking
    private User user; // Utente che ha effettuato la prenotazione

    // Rimosso: private String bookedByName;
    private LocalDateTime bookedAt;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int duration; // in ore
    private String reason;
    private boolean active = true;
    private boolean reminderSent = false; // Nuovo campo

    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        // Controlla se this.startDateTime e this.endDateTime sono nulli prima di chiamare isBefore/isAfter
        if (this.startDateTime == null || this.endDateTime == null) {
            return false; // o gestisci come preferisci se le date possono essere nulle
        }
        return !(end.isBefore(this.startDateTime) || start.isAfter(this.endDateTime));
    }

    @Transient
    public CarInfo getCarInfo() {
        return car != null ? new CarInfo(car.getId(), car.getName(), car.getSeats()) : null;
    }

    // Potresti aggiungere un metodo transiente simile per UserInfo se necessario per i DTO
    // @Transient
    // public UserInfo getUserInfo() {
    //     return user != null ? new UserInfo(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName()) : null;
    // }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }
}