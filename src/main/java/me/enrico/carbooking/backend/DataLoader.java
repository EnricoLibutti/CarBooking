package me.enrico.carbooking.backend;

import me.enrico.carbooking.model.Car;
import me.enrico.carbooking.model.Role;
import me.enrico.carbooking.model.User;
import me.enrico.carbooking.repositories.CarRepository;
import me.enrico.carbooking.repositories.UserRepository; // Aggiungi import
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Aggiungi import

import java.util.Set; // Aggiungi import

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(CarRepository carRepository,
                                   UserRepository userRepository, // Inietta UserRepository
                                   PasswordEncoder passwordEncoder) { // Inietta PasswordEncoder
        return args -> {
            // Caricamento auto di default
            if (carRepository.count() == 0) {
                carRepository.save(new Car("Mini Nera", 4));
                carRepository.save(new Car("Mini Bianca", 4));
                carRepository.save(new Car("Opel Mokka", 5));
            }

            // Caricamento utenti di default
            if (userRepository.count() == 0) {
                User enrico = new User(
                        "enrico",
                        "zrageyh@gmail.com", // Aggiunto email
                        passwordEncoder.encode("enrico"),
                        "Enrico",
                        "Libutti",
                        Set.of(Role.ROLE_ADMIN, Role.ROLE_USER)
                );
                userRepository.save(enrico);

                User claudio = new User(
                        "claudio",
                        "claudiolibutti@gmail.com", // Aggiunto email
                        passwordEncoder.encode("pippo123"), // Codifica la password
                        "Claudio",
                        "Libutti",
                        Set.of(Role.ROLE_USER)
                );
                userRepository.save(claudio);

                // Aggiungi altri membri della famiglia come utenti
                User jordi = new User(
                        "jordi",
                        "libuttijordi@gmail.com", // Aggiunto email
                        passwordEncoder.encode("password123"),
                        "Jordi",
                        "Libutti",
                        Set.of(Role.ROLE_USER)
                );
                userRepository.save(jordi);

                User sergio = new User(
                        "sergio",
                        "ser", // Aggiunto email
                        passwordEncoder.encode("password123"),
                        "Sergio",
                        "Libutti",
                        Set.of(Role.ROLE_USER)
                );
                userRepository.save(sergio);
            }
        };
    }
}
