package me.enrico.carbooking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int seats;
    private boolean available;

    @JsonManagedReference
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    public Car() {}

    public Car(String name, int seats) {
        this.name = name;
        this.seats = seats;
        this.available = true;
    }
}